/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

import akka.util.Timeout
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.hmrc.lightweightcontactevents.connectors.VoaDataTransferConnector
import uk.gov.hmrc.lightweightcontactevents.repository.QueuedDataTransferRepository

import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito.Mockito.{times, verify, when}
import org.mockito.Matchers.any
import org.mockito.Matchers.{eq => eqTo}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import reactivemongo.api.commands.{GetLastError, WriteResult}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, PropertyAddress, QueuedDataTransfer, VOADataTransfer}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Success, Try}

class VoaDataTransferExporterSpec extends FlatSpec with Matchers with MockitoSugar with FutureAwaits
  with DefaultAwaitTimeout {
  val now = Instant.now()

  val nowMinus12Days = now.minusSeconds(60 * 60 * 24 * 12)

  val clock = new Clock {
    var now = Instant.now()
    override def getZone: ZoneId = ZoneId.systemDefault()

    override def withZone(zone: ZoneId): Clock = ???

    override def instant(): Instant = now
  }


  "DataExporter" should "export data" in {
    val dataTransferConnector = mock[VoaDataTransferConnector]

    val dataTransferRepository = mock[QueuedDataTransferRepository]

    val voaDataTransferExporter = new VoaDataTransferExporter(dataTransferConnector, dataTransferRepository, clock)

    val transfer = aQueuedDataTransfer()
    val data = List(transfer)

    when(dataTransferConnector.transfer(any(classOf[VOADataTransfer]))(any(classOf[HeaderCarrier]))).thenReturn(Future.successful(Success(200)))
    when(dataTransferRepository.findBatch()).thenReturn(Future.successful(data))
    when(dataTransferRepository.removeById(any(classOf[BSONObjectID]), any(classOf[GetLastError]))(any(classOf[ExecutionContext]))).thenReturn(Future.successful(mock[WriteResult]))

    await(voaDataTransferExporter.exportBatch())

    verify(dataTransferConnector, times(1)).transfer(eqTo(transfer.voaDataTransfer))(any(classOf[HeaderCarrier]))
    verify(dataTransferRepository, times(1)).removeById(eqTo(transfer.id), any(classOf[GetLastError]))(any(classOf[ExecutionContext]))

  }

  it should "record error" in {
    val dataTransferConnector = mock[VoaDataTransferConnector]

    val dataTransferRepository = mock[QueuedDataTransferRepository]

    val voaDataTransferExporter = new VoaDataTransferExporter(dataTransferConnector, dataTransferRepository, clock)

    val transfer = aQueuedDataTransfer()
    val data = List(transfer)

    when(dataTransferConnector.transfer(any(classOf[VOADataTransfer]))(any(classOf[HeaderCarrier]))).thenReturn(Future.successful(Success(404)))
    when(dataTransferRepository.findBatch()).thenReturn(Future.successful(data))
    when(dataTransferRepository.removeById(any(classOf[BSONObjectID]), any(classOf[GetLastError]))(any(classOf[ExecutionContext]))).thenReturn(Future.successful(mock[WriteResult]))
    when(dataTransferRepository.updateTime(any(classOf[BSONObjectID]), any(classOf[Instant]))(any(classOf[ExecutionContext]))).thenReturn(Future.successful(()))

    await(voaDataTransferExporter.exportBatch())

    verify(dataTransferConnector, times(1)).transfer(eqTo(transfer.voaDataTransfer))(any(classOf[HeaderCarrier]))
    verify(dataTransferRepository, times(0)).removeById(eqTo(transfer.id), any(classOf[GetLastError]))(any(classOf[ExecutionContext]))

    verify(dataTransferRepository, times(1)).updateTime(eqTo(transfer.id), eqTo(clock.now))(any(classOf[ExecutionContext]))

  }

  it should "remove element with permanent error" in {
    val dataTransferConnector = mock[VoaDataTransferConnector]

    val dataTransferRepository = mock[QueuedDataTransferRepository]

    val voaDataTransferExporter = new VoaDataTransferExporter(dataTransferConnector, dataTransferRepository, clock)

    val transfer = aQueuedDataTransfer().copy(fistError = Option(nowMinus12Days))
    val data = List(transfer)

    when(dataTransferConnector.transfer(any(classOf[VOADataTransfer]))(any(classOf[HeaderCarrier]))).thenReturn(Future.successful(Success(404)))
    when(dataTransferRepository.findBatch()).thenReturn(Future.successful(data))
    when(dataTransferRepository.removeById(any(classOf[BSONObjectID]), any(classOf[GetLastError]))(any(classOf[ExecutionContext]))).thenReturn(Future.successful(mock[WriteResult]))
    when(dataTransferRepository.updateTime(any(classOf[BSONObjectID]), any(classOf[Instant]))(any(classOf[ExecutionContext]))).thenReturn(Future.successful(()))

    await(voaDataTransferExporter.exportBatch())

    verify(dataTransferConnector, times(0)).transfer(eqTo(transfer.voaDataTransfer))(any(classOf[HeaderCarrier]))
    verify(dataTransferRepository, times(1)).removeById(eqTo(transfer.id), any(classOf[GetLastError]))(any(classOf[ExecutionContext]))

    verify(dataTransferRepository, times(0)).updateTime(eqTo(transfer.id), eqTo(clock.now))(any(classOf[ExecutionContext]))

  }

  def aQueuedDataTransfer() = {
    QueuedDataTransfer(aVoaDataTransfer())
  }

  def aVoaDataTransfer() = {
    VOADataTransfer(aConfirmedContactDetails(), aPropertyAddress(), true,
      "Subject", "email@email.com", "category", "subCategory", "Free text message")
  }

  def aPropertyAddress() = {
    PropertyAddress("Some stree", None, "Some town", Some("Some county"), "BN12 4AX")
  }

  def aConfirmedContactDetails()  = {
    ConfirmedContactDetails(
      "John",
      "Doe",
      "email@noreply.voa.gov.uk",
      "0123456789"
    )
  }


}
