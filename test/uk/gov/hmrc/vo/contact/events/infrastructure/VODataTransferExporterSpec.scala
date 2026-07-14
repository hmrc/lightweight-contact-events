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

import org.mongodb.scala.bson.ObjectId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vo.contact.events.utils.LightweightFixture.*
import uk.gov.hmrc.vo.contact.events.connectors.NotifyConnector
import uk.gov.hmrc.vo.contact.events.models.VODataTransfer
import uk.gov.hmrc.vo.contact.events.repository.QueuedDataTransferRepository
import uk.gov.hmrc.vo.unit.test.BaseAppSpec

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future
import scala.util.{Failure, Success}

class VODataTransferExporterSpec extends BaseAppSpec:

  private val now: Instant            = Instant.now
  private val nowMinus12Days: Instant = now.minusSeconds(60 * 60 * 24 * 12)

  private val clock: Clock =
    new Clock:
      private val now = Instant.now

      override def getZone: ZoneId = ZoneId.systemDefault

      override def withZone(zone: ZoneId): Clock = ???

      override def instant(): Instant = now

  "VODataTransferExporter" should {
    "export data" in {
      val notifyConnector = mock[NotifyConnector]

      val dataTransferRepository = mock[QueuedDataTransferRepository]

      val voDataTransferExporter = VODataTransferExporter(notifyConnector, dataTransferRepository, clock)

      val transfer = aQueuedDataTransfer()
      val data     = List(transfer)

      when(notifyConnector.sendEmailToVO(any[VODataTransfer])(using any[HeaderCarrier]))
        .thenReturn(Success(()))
      when(dataTransferRepository.findBatch()).thenReturn(Future.successful(data))
      when(dataTransferRepository.removeById(any[ObjectId])).thenReturn(Future.unit)

      voDataTransferExporter.exportBatch().futureValue

      verify(notifyConnector, times(1)).sendEmailToVO(eqTo(transfer.voDataTransfer))(using any[HeaderCarrier])
      verify(dataTransferRepository, times(1)).removeById(eqTo(transfer._id))
    }

    "record error" in {
      val notifyConnector = mock[NotifyConnector]

      val dataTransferRepository = mock[QueuedDataTransferRepository]

      val voDataTransferExporter = VODataTransferExporter(notifyConnector, dataTransferRepository, clock)

      val transfer = aQueuedDataTransfer()
      val data     = List(transfer)

      when(notifyConnector.sendEmailToVO(any[VODataTransfer])(using any[HeaderCarrier]))
        .thenReturn(Failure(RuntimeException("Send email failure")))
      when(dataTransferRepository.findBatch()).thenReturn(Future.successful(data))
      when(dataTransferRepository.removeById(any[ObjectId])).thenReturn(Future.unit)
      when(dataTransferRepository.updateTime(any[ObjectId], any[Instant])).thenReturn(Future.unit)

      voDataTransferExporter.exportBatch().futureValue

      verify(notifyConnector, times(1)).sendEmailToVO(eqTo(transfer.voDataTransfer))(using any[HeaderCarrier])
      verify(dataTransferRepository, times(0)).removeById(eqTo(transfer._id))

      verify(dataTransferRepository, times(1)).updateTime(eqTo(transfer._id), eqTo(clock.instant()))
    }

    "remove element with permanent error" in {
      val notifyConnector = mock[NotifyConnector]

      val dataTransferRepository = mock[QueuedDataTransferRepository]

      val voDataTransferExporter = VODataTransferExporter(notifyConnector, dataTransferRepository, clock)

      val transfer = aQueuedDataTransfer().copy(firstError = Option(nowMinus12Days))
      val data     = List(transfer)

      when(notifyConnector.sendEmailToVO(any[VODataTransfer])(using any[HeaderCarrier]))
        .thenReturn(Failure(RuntimeException("Send email failure")))
      when(dataTransferRepository.findBatch()).thenReturn(Future.successful(data))
      when(dataTransferRepository.removeById(any[ObjectId])).thenReturn(Future.unit)
      when(dataTransferRepository.updateTime(any[ObjectId], any[Instant])).thenReturn(Future.unit)

      voDataTransferExporter.exportBatch().futureValue

      verify(notifyConnector, times(0)).sendEmailToVO(eqTo(transfer.voDataTransfer))(using any[HeaderCarrier])
      verify(dataTransferRepository, times(1)).removeById(eqTo(transfer._id))

      verify(dataTransferRepository, times(0)).updateTime(eqTo(transfer._id), eqTo(clock.instant()))
    }
  }
