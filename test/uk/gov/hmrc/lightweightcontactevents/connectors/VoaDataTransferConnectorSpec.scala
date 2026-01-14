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

package uk.gov.hmrc.lightweightcontactevents.connectors

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.Environment
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.lightweightcontactevents.models.ConfirmedContactDetails.toLegacyContact
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, PropertyAddress, VOADataTransfer}
import uk.gov.hmrc.lightweightcontactevents.{RequestBuilderStub, SpecBase}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class VoaDataTransferConnectorSpec extends SpecBase {

  implicit val ec: ExecutionContext  = injector.instanceOf[ExecutionContext]
  val auditService: AuditingService  = injector.instanceOf[AuditingService]
  val environment: Environment       = injector.instanceOf[Environment]
  val servicesConfig: ServicesConfig = injector.instanceOf[ServicesConfig]

  val message                                          = "MSG"
  val subject                                          = "Valuation Office Agency Contact Form"
  val ctEmail                                          = "ct.email@voa.gsi.gov.uk"
  val ndrEmail                                         = "ndr.email@voa.gsi.gov.uk"
  val enquiryCategoryMsg                               = "Council Tax"
  val subEnquiryCategoryMsg                            = "My property is in poor repair or uninhabitable"
  val contactReason                                    = "more_details"
  val confirmedContactDetails: ConfirmedContactDetails = ConfirmedContactDetails("full name", "email", "07777777")
  val propertyAddress: PropertyAddress                 = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")

  val ctDataTransfer: VOADataTransfer =
    VOADataTransfer(toLegacyContact(confirmedContactDetails), propertyAddress, true, subject, ctEmail, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val minimalJson: JsValue            = Json.toJson(ctDataTransfer)

  def connector(httpClientV2Mock: HttpClientV2) =
    new VoaDataTransferConnector(httpClientV2Mock, auditService, servicesConfig)

  "Voa Data Transfer Connector" when {

    "provided with Voa Data Transfer Model" must {

      "Send the contact details returning a 200 when it succeeds" in {
        val httpMock = getHttpMock(OK)
        val result   = await(connector(httpMock).transfer(ctDataTransfer)(using HeaderCarrier()))

        result match {
          case Success(status) => status mustBe OK
          case Failure(_)      => assert(false)
        }
      }

      "return a failure representing the error when send method fails" in {
        val httpMock = getHttpMock(INTERNAL_SERVER_ERROR)
        val result   = await(connector(httpMock).transfer(ctDataTransfer)(using HeaderCarrier()))

        assert(result.isFailure)
      }
    }

    "provided with JSON directly" must {
      "call the Microservice with the given JSON" in {
        val headerCarrierNapper = ArgumentCaptor.forClass(classOf[HeaderCarrier])
        val urlCaptor           = ArgumentCaptor.forClass(classOf[URL])

        val httpMock          = getHttpMock(OK)
        val headerCarrierStub = HeaderCarrier()
        connector(httpMock).sendJson(minimalJson)(using headerCarrierStub)

        verify(httpMock).post(urlCaptor.capture)(using headerCarrierNapper.capture)
        urlCaptor.getValue.toString must endWith("contact-process-api/contact/sendemail")
        headerCarrierNapper.getValue.nsStamp mustBe headerCarrierStub.nsStamp
      }

      "return a 200 if the data transfer call is successful" in {
        val result = await(connector(getHttpMock(OK)).sendJson(minimalJson)(using HeaderCarrier()))
        result match {
          case Success(status) =>
            status mustBe OK
          case _               => assert(false)
        }
      }

      "throw an failure if the data transfer call fails" in {
        val httpMock = getHttpMock(INTERNAL_SERVER_ERROR)
        val result   = await(connector(httpMock).sendJson(minimalJson)(using HeaderCarrier()))
        assert(result.isFailure)
      }

      "return a failure if the data transfer call throws an exception" in {
        val httpClientV2Mock = mock[HttpClientV2]
        when(
          httpClientV2Mock.post(any[URL])(using any[HeaderCarrier])
        ).thenReturn(RequestBuilderStub(Left(new RuntimeException), "{}"))

        val result = await(connector(httpClientV2Mock).sendJson(minimalJson)(using HeaderCarrier()))
        assert(result.isFailure)
      }
    }
  }

}
