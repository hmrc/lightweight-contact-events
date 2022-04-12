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

package uk.gov.hmrc.lightweightcontactevents.connectors

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, anyString}
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import uk.gov.hmrc.lightweightcontactevents.models.ConfirmedContactDetails.toLegacyContact
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, PropertyAddress, VOADataTransfer}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.Future
import scala.util.{Failure, Success}

class VoaDataTransferConnectorSpec extends SpecBase {

  val configuration = injector.instanceOf[Configuration]
  val auditService = injector.instanceOf[AuditingService]
  val environment = injector.instanceOf[Environment]
  val servicesConfig = injector.instanceOf[ServicesConfig]

  val message = "MSG"
  val subject = "Valuation Office Agency Contact Form"
  val ctEmail = "ct.email@voa.gsi.gov.uk"
  val ndrEmail = "ndr.email@voa.gsi.gov.uk"
  val enquiryCategoryMsg = "Council Tax"
  val subEnquiryCategoryMsg = "My property is in poor repair or uninhabitable"
  val contactReason = "more_details"
  val confirmedContactDetails = ConfirmedContactDetails("full name", "email", "07777777")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val ctDataTransfer = VOADataTransfer(toLegacyContact(confirmedContactDetails), propertyAddress, true, subject, ctEmail,
    enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val minimalJson = Json.toJson(ctDataTransfer)

  "Voa Data Transfer Connector" when {

    "provided with Voa Data Transfer Model" must {

      "Send the contact details returning a 200 when it succeeds" in {
        val httpMock = getHttpMock(200)
        val connector = new VoaDataTransferConnector(httpMock, configuration, environment,auditService, servicesConfig)

        val result = await(connector.transfer(ctDataTransfer)(HeaderCarrier()))

        result match {
          case Success(status) => status mustBe 200
          case Failure(_) => assert(false)
        }
      }

      "return a failure representing the error when send method fails" in {
        val httpMock = getHttpMock(500)
        val connector = new VoaDataTransferConnector(httpMock, configuration, environment,auditService, servicesConfig)
        val result = await(connector.transfer(ctDataTransfer)(HeaderCarrier()))

        assert(result.isFailure)
      }
    }

    "provided with JSON directly" must {
      "call the Microservice with the given JSON" in {
        implicit val headerCarrierNapper = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        implicit val httpReadsNapper = ArgumentCaptor.forClass(classOf[HttpReads[Any]])
        implicit val jsonWritesNapper = ArgumentCaptor.forClass(classOf[Writes[JsValue]])
        val urlCaptor = ArgumentCaptor.forClass(classOf[String])
        val bodyCaptor = ArgumentCaptor.forClass(classOf[JsValue])
        val headersCaptor = ArgumentCaptor.forClass(classOf[Seq[(String, String)]])
        val httpMock = getHttpMock(200)

        val connector = new VoaDataTransferConnector(httpMock, configuration, environment,auditService, servicesConfig)
        connector.sendJson(minimalJson)(HeaderCarrier())

        verify(httpMock).POST(urlCaptor.capture, bodyCaptor.capture, headersCaptor.capture)(jsonWritesNapper.capture,
          httpReadsNapper.capture, headerCarrierNapper.capture, any())
        urlCaptor.getValue must endWith("contact-process-api/contact/sendemail")
        bodyCaptor.getValue mustBe minimalJson
        headersCaptor.getValue mustBe Seq(connector.jsonContentTypeHeader)
      }

      "return a 200 if the data transfer call is successful" in {
        val connector = new VoaDataTransferConnector(getHttpMock(200), configuration, environment,auditService, servicesConfig)
        val result = await(connector.sendJson(minimalJson)(HeaderCarrier()))
        result match {
          case Success(status) =>
            status mustBe 200
          case _ => assert(false)
        }
      }

      "throw an failure if the data transfer call fails" in {
        val connector = new VoaDataTransferConnector(getHttpMock(500), configuration, environment,auditService, servicesConfig)
        val result = await(connector.sendJson(minimalJson)(HeaderCarrier()))
        assert(result.isFailure)
      }

      "return a failure if the data transfer call throws an exception" in {
        val httpMock = mock[HttpClient]
        when(httpMock.POST(anyString, any[JsValue], any[Seq[(String, String)]])(any[Writes[JsValue]], any[HttpReads[Any]],
          any[HeaderCarrier], any())) thenReturn Future.successful(new RuntimeException)
        val connector = new VoaDataTransferConnector(httpMock, configuration, environment,auditService, servicesConfig)
        val result = await(connector.sendJson(minimalJson)(HeaderCarrier()))
        assert(result.isFailure)
      }
    }
  }
}

