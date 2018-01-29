/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.lightweightcontactevents.connectors

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.{verify, when}
import org.scalatest.Assertion
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, Contact, PropertyAddress, VOADataTransfer}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import play.api.test.Helpers.{status, _}

class VoaDataTransferConnectorSpec extends SpecBase {

  val configuration = injector.instanceOf[Configuration]
  val environment = injector.instanceOf[Environment]

  val message = "MSG"
  val enquiryCategoryMsg = "Council Tax"
  val subEnquiryCategoryMsg = "My property is in poor repair or uninhabitable"
  val confirmedContactDetails = ConfirmedContactDetails("first", "last", "email", "07777777")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val ctContact = Contact(confirmedContactDetails, propertyAddress, true, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val ctDataTransfer = VOADataTransfer(ctContact)

  val minimalJson = Json.toJson(ctDataTransfer)


  "Voa Data Transfer Connector" when {

    "provided with Voa Data Transfer Model" must {

      "Send the contact details returning a 200 when it succeeds" in {
        val httpMock = getHttpMock(202)
        val connector = new VoaDataTransferConnector(httpMock, configuration, environment)

        val result = await(connector.transfer(ctDataTransfer))

        result match {
          case Success(status) => status mustBe 200
          case Failure(_) => assert(false)
        }
      }

      "return a failure representing the error when send method fails" in {
        val httpMock = getHttpMock(500)
        val connector = new VoaDataTransferConnector(httpMock, configuration, environment)
        val result = await(connector.transfer(ctDataTransfer))

        assert(result.isFailure)
      }
    }

    "provided with JSON directly" must {
      "call the Microservice with the given JSON" in {
        implicit val headerCarrierNapper = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        implicit val httpReadsNapper = ArgumentCaptor.forClass(classOf[HttpReads[Any]])
        implicit val jsonWritesNapper = ArgumentCaptor.forClass(classOf[Writes[Any]])
        val urlCaptor = ArgumentCaptor.forClass(classOf[String])
        val bodyCaptor = ArgumentCaptor.forClass(classOf[JsValue])
        val headersCaptor = ArgumentCaptor.forClass(classOf[Seq[(String, String)]])
        val httpMock = getHttpMock(200)

        val connector = new VoaDataTransferConnector(httpMock, configuration, environment)
        connector.sendJson(minimalJson)

        verify(httpMock).POST(urlCaptor.capture, bodyCaptor.capture, headersCaptor.capture)(jsonWritesNapper.capture,
          httpReadsNapper.capture, headerCarrierNapper.capture, any())
        urlCaptor.getValue must endWith("contact")
        bodyCaptor.getValue mustBe minimalJson
        headersCaptor.getValue mustBe Seq(connector.jsonContentTypeHeader)
      }

      "return a 200 if the data transfer call is successful" in {
        val connector = new VoaDataTransferConnector(getHttpMock(202), configuration, environment)
        val result = await(connector.sendJson(minimalJson))
        result match {
          case Success(status) =>
            status mustBe 200
          case _ => assert(false)
        }
      }

      "throw an failure if the data transfer call fails" in {
        val connector = new VoaDataTransferConnector(getHttpMock(500), configuration, environment)
        val result = await(connector.sendJson(minimalJson))
        assert(result.isFailure)
      }

      "return a failure if the data transfer call throws an exception" in {
        val httpMock = mock[HttpClient]
        when(httpMock.POST(anyString, any[JsValue], any[Seq[(String, String)]])(any[Writes[Any]], any[HttpReads[Any]],
          any[HeaderCarrier], any())) thenReturn Future.successful(new RuntimeException)
          val connector = new VoaDataTransferConnector(httpMock, configuration, environment)
          val result = await(connector.sendJson(minimalJson))
          assert(result.isFailure)
        }
      }
    }
}

