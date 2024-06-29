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

import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.mongodb.scala.bson.ObjectId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lightweightcontactevents.connectors.VoaDataTransferConnector
import uk.gov.hmrc.lightweightcontactevents.models.VOADataTransfer
import uk.gov.hmrc.lightweightcontactevents.repository.QueuedDataTransferRepository
import uk.gov.hmrc.lightweightcontactevents.utils.LightweightFixture.*

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class VoaDataTransferExporterSpec extends AnyFlatSpec with Matchers with MockitoSugar with FutureAwaits with DefaultAwaitTimeout {
  val now: Instant = Instant.now()

  val nowMinus12Days: Instant = now.minusSeconds(60 * 60 * 24 * 12)

  val clock: Clock = new Clock {
    private val now              = Instant.now()
    override def getZone: ZoneId = ZoneId.systemDefault()

    override def withZone(zone: ZoneId): Clock = ???

    override def instant(): Instant = now
  }

  "DataExporter" should "export data" in {
    val dataTransferConnector = mock[VoaDataTransferConnector]

    val dataTransferRepository = mock[QueuedDataTransferRepository]

    val voaDataTransferExporter = new VoaDataTransferExporter(dataTransferConnector, dataTransferRepository, clock)

    val transfer = aQueuedDataTransfer()
    val data     = List(transfer)

    when(dataTransferConnector.transfer(any[VOADataTransfer])(any[HeaderCarrier])) thenReturn Future.successful(Success(OK))
    when(dataTransferRepository.findBatch()) thenReturn Future.successful(data)
    when(dataTransferRepository.removeById(any[ObjectId])) thenReturn Future.unit

    await(voaDataTransferExporter.exportBatch())

    verify(dataTransferConnector, times(1)).transfer(eqTo(transfer.voaDataTransfer))(any[HeaderCarrier])
    verify(dataTransferRepository, times(1)).removeById(eqTo(transfer._id))
  }

  it should "record error" in {
    val dataTransferConnector = mock[VoaDataTransferConnector]

    val dataTransferRepository = mock[QueuedDataTransferRepository]

    val voaDataTransferExporter = new VoaDataTransferExporter(dataTransferConnector, dataTransferRepository, clock)

    val transfer = aQueuedDataTransfer()
    val data     = List(transfer)

    when(dataTransferConnector.transfer(any[VOADataTransfer])(any[HeaderCarrier])) thenReturn Future.successful(Success(NOT_FOUND))
    when(dataTransferRepository.findBatch()) thenReturn Future.successful(data)
    when(dataTransferRepository.removeById(any[ObjectId])) thenReturn Future.unit
    when(dataTransferRepository.updateTime(any[ObjectId], any[Instant])) thenReturn Future.unit

    await(voaDataTransferExporter.exportBatch())

    verify(dataTransferConnector, times(1)).transfer(eqTo(transfer.voaDataTransfer))(any[HeaderCarrier])
    verify(dataTransferRepository, times(0)).removeById(eqTo(transfer._id))

    verify(dataTransferRepository, times(1)).updateTime(eqTo(transfer._id), eqTo(clock.instant()))
  }

  it should "remove element with permanent error" in {
    val dataTransferConnector = mock[VoaDataTransferConnector]

    val dataTransferRepository = mock[QueuedDataTransferRepository]

    val voaDataTransferExporter = new VoaDataTransferExporter(dataTransferConnector, dataTransferRepository, clock)

    val transfer = aQueuedDataTransfer().copy(firstError = Option(nowMinus12Days))
    val data     = List(transfer)

    when(dataTransferConnector.transfer(any[VOADataTransfer])(any[HeaderCarrier])) thenReturn Future.successful(Success(NOT_FOUND))
    when(dataTransferRepository.findBatch()) thenReturn Future.successful(data)
    when(dataTransferRepository.removeById(any[ObjectId])) thenReturn Future.unit
    when(dataTransferRepository.updateTime(any[ObjectId], any[Instant])) thenReturn Future.unit

    await(voaDataTransferExporter.exportBatch())

    verify(dataTransferConnector, times(0)).transfer(eqTo(transfer.voaDataTransfer))(any[HeaderCarrier])
    verify(dataTransferRepository, times(1)).removeById(eqTo(transfer._id))

    verify(dataTransferRepository, times(0)).updateTime(eqTo(transfer._id), eqTo(clock.instant()))
  }

}
