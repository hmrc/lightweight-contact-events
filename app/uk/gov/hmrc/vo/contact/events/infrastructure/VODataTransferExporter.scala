/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vo.contact.events.infrastructure

import java.time.{Clock, Duration, Instant}
import javax.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vo.contact.events.connectors.VODataTransferConnector
import uk.gov.hmrc.vo.contact.events.models.{QueuedDataTransfer, VODataTransfer}
import uk.gov.hmrc.vo.contact.events.repository.QueuedDataTransferRepository

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

@Singleton
class VODataTransferExporter @Inject() (
  dataTransferConnector: VODataTransferConnector,
  dataTransferRepository: QueuedDataTransferRepository,
  clock: Clock = Clock.systemDefaultZone()
) extends Logging:

  private val sevenDaysSeconds = 7L * 24L * 60L * 60L

  def exportBatch()(using ec: ExecutionContext): Future[Unit] =
    dataTransferRepository.findBatch().flatMap { seq =>
      logger.info(s"Found ${seq.size} transfer(s) to export")
      processSequentially(seq)
    }

  private def processSequentially(transfers: Seq[QueuedDataTransfer])(using ec: ExecutionContext): Future[Unit] =
    if transfers.isEmpty then
      Future.unit
    else
      processTransfer(transfers.head).flatMap(_ => processSequentially(transfers.tail))

  private def processTransfer(transfer: QueuedDataTransfer)(using ec: ExecutionContext): Future[Unit] =
    transfer.firstError match
      // if error is permanent, remove element
      case Some(x) if Duration.between(x, Instant.now(clock)).getSeconds > sevenDaysSeconds => removeTransferWithError(transfer)
      case _                                                                                =>
        val promise = Promise[Unit]()
        sendToVO(transfer.voDataTransfer).onComplete {
          case Success(_) =>
            dataTransferRepository.removeById(transfer._id).onComplete(x => promise.complete(x.map(_ => ())))
          case Failure(_) =>
            recordError(transfer).onComplete(x => promise.complete(x.map(_ => ())))
        }
        promise.future

  private def removeTransferWithError(transfer: QueuedDataTransfer)(using ec: ExecutionContext): Future[Unit] =
    logger.warn(s"removing element with permanent error : $transfer") // TODO - send details only to SPLUNK
    dataTransferRepository.removeById(transfer._id).map(_ => ())

  private def sendToVO(transfer: VODataTransfer)(using ec: ExecutionContext): Future[Unit] =
    val hc: HeaderCarrier = HeaderCarrier()
    dataTransferConnector.transfer(transfer)(using hc).flatMap {
      case Success(statusCode) if statusCode < 300 => Future.unit
      case Success(statusCode)                     => Future.failed(RuntimeException(s"Unable to send data to VO, StatusCode: $statusCode"))
      case Failure(exception)                      => Future.failed(RuntimeException("Unable to send data to VO", exception))
    }

  private def recordError(transfer: QueuedDataTransfer): Future[Unit] =
    if transfer.firstError.isEmpty then
      dataTransferRepository.updateTime(transfer._id, Instant.now(clock))
    else
      Future.unit
