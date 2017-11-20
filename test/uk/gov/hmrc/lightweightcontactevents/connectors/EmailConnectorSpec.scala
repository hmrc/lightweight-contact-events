/*
 * Copyright 2017 HM Revenue & Customs
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
import org.scalatest.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, Contact, Email, PropertyAddress}
import uk.gov.hmrc.lightweightcontactevents.utils.Initialize
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class EmailConnectorSpec extends SpecBase with MockitoSugar {

  val configuration = injector.instanceOf[Configuration]
  val environment = injector.instanceOf[Environment]
  val init = injector.instanceOf[Initialize]
  val message = "MSG"
  val enquiryCategoryMsg = "Council Tax"
  val subEnquiryCategoryMsg = "SEC"
  val confirmedContactDetails = ConfirmedContactDetails("first", "last", "email", "07777777")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val contact = Contact(confirmedContactDetails, propertyAddress, true, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val email = Email(contact, init)
  val minimalJson = Json.toJson(email)

  def getHttpMock(returnedStatus: Int): HttpClient = {
    val httpMock = mock[HttpClient]
    when(httpMock.POST(anyString, any[JsValue], any[Seq[(String, String)]])(any[Writes[Any]], any[HttpReads[Any]],
      any[HeaderCarrier], any())) thenReturn Future.successful(HttpResponse(returnedStatus, None))
    when(httpMock.GET(anyString)(any[HttpReads[Any]], any[HeaderCarrier], any())) thenReturn Future.successful(HttpResponse(returnedStatus, None))
    httpMock
  }

  "EmailConnector" when {
    "provided with Email model input" must {
      "Send the email returning a 200 when the email service succeeds" in {
        val httpMock = getHttpMock(202)
        val connector = new EmailConnector(httpMock, configuration, environment)
        connector.sendEmail(email).map {
          case Success(status) => status mustBe 200
          case Failure(_) => assert(false)
        }
      }

      "return a failure representing the error when send method fails" in {
        val httpMock = getHttpMock(500)
        val connector = new EmailConnector(httpMock, configuration, environment)
        connector.sendEmail(email).map { result =>
          assert(result.isFailure)
        }
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

        val connector = new EmailConnector(httpMock, configuration, environment)
        connector.sendJson(minimalJson)

        verify(httpMock).POST(urlCaptor.capture, bodyCaptor.capture, headersCaptor.capture)(jsonWritesNapper.capture,
          httpReadsNapper.capture, headerCarrierNapper.capture, any())
        urlCaptor.getValue must endWith(s"${connector.domain}email")
        bodyCaptor.getValue mustBe minimalJson
        headersCaptor.getValue mustBe Seq(connector.jsonContentTypeHeader)
      }

      "return a 200 if the email service call is successful" in {
        new EmailConnector(getHttpMock(202), configuration, environment).sendJson(minimalJson).map { status =>
          status mustBe Success(200)
        }
      }

      "throw an failure if the email service call fails" in {
          new EmailConnector(getHttpMock(500), configuration, environment).sendJson(minimalJson). map { f =>
            assert(f.isFailure)
        }
      }

      "return a failure if the email service call throws an exception" in {
        val httpMock = mock[HttpClient]
        when(httpMock.POST(anyString, any[JsValue], any[Seq[(String, String)]])(any[Writes[Any]], any[HttpReads[Any]],
          any[HeaderCarrier], any())) thenReturn  Future.successful(new RuntimeException)
        new EmailConnector(httpMock, configuration, environment).sendJson(minimalJson). map {f =>
          assert(f.isFailure)
        }
      }
    }
  }
}
