/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.lightweightcontactevents.infrastructure

import java.time.{Clock, Duration, Instant}

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lightweightcontactevents.connectors.VoaDataTransferConnector
import uk.gov.hmrc.lightweightcontactevents.models.{QueuedDataTransfer, VOADataTransfer}
import uk.gov.hmrc.lightweightcontactevents.repository.QueuedDataTransferRepository

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

@Singleton
class VoaDataTransferExporter @Inject() (dataTransferConnector: VoaDataTransferConnector,
                                         dataTransferRepository: QueuedDataTransferRepository,
                                         clock: Clock = Clock.systemDefaultZone()
                                        ) {

  val sevenDaysSeconds = 7L * 24L * 60L * 60L

  def exportBatch()(implicit ec: ExecutionContext): Future[Unit] = {
    dataTransferRepository.findBatch().flatMap(x => {
      Logger(getClass).info(s"Found ${x.size} transfer(s) to export")
      processSequentially(x)})
  }

  private def processSequentially(transfers: List[QueuedDataTransfer])(implicit ec: ExecutionContext): Future[Unit] =
    if (transfers.isEmpty) {
      Future.unit
    } else {
      processTransfer(transfers.head).flatMap(_ => processSequentially(transfers.tail))
    }

  def processTransfer(transfer: QueuedDataTransfer)(implicit ec: ExecutionContext): Future[Unit] = {
    transfer.fistError match {
        //if error is permanent, remove element
      case Some(x) if (Duration.between(x, Instant.now(clock)).getSeconds() > sevenDaysSeconds) => removeTransferWithError(transfer)
      case _ => {
        val promise = Promise[Unit]()
        sendToVoa(transfer.voaDataTransfer).onComplete {
          case Success(_) => {
            dataTransferRepository.removeById(transfer.id).onComplete(x => promise.complete(x.map(_ =>())))
          }case Failure(_) => {
            recordError(transfer).onComplete(x => promise.complete(x.map(_ => ())))
          }
        }
        promise.future
      }
    }
  }

  def removeTransferWithError(transfer: QueuedDataTransfer)(implicit ec: ExecutionContext): Future[Unit] = {
    Logger(getClass).warn(s"removing element with permanent error : ${transfer}")//TODO - send details only to SPLUNK
    dataTransferRepository.removeById(transfer.id).map(_ => ())
  }

  def sendToVoa(transfer: VOADataTransfer)(implicit ec: ExecutionContext): Future[Unit] = {
    val hc: HeaderCarrier = new HeaderCarrier()
    dataTransferConnector.transfer(transfer)(hc).flatMap {
      case Success(statusCode) if statusCode < 300 => Future.unit
      case Success(statusCode) => Future.failed(new RuntimeException(s"Unable to send data to VOA, StatusCode: ${statusCode}"))
      case Failure(exception) => Future.failed(new RuntimeException(s"Unable to send data to VOA", exception))
    }
  }

  private def recordError(transfer: QueuedDataTransfer): Future[Unit] =
    if (transfer.fistError.isEmpty) {
      dataTransferRepository.updateTime(transfer.id, Instant.now(clock))
    } else {
      Future.unit
    }

}
