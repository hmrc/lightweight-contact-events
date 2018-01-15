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

package uk.gov.hmrc.lightweightcontactevents.controllers


import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import uk.gov.hmrc.lightweightcontactevents.connectors.EmailConnector
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, Contact, Email, PropertyAddress}
import uk.gov.hmrc.lightweightcontactevents.utils.Initialize
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class CreationControllerSpec extends SpecBase with MockitoSugar {

  def fakeRequestWithJson(jsonStr: String) = {
    val json = Json.parse(jsonStr)
    FakeRequest("POST", "").withHeaders("Content-Type" -> "application/json").withJsonBody(json)
  }

  val contactJson =
    """{
    "contact": {
      "firstName": "first",
      "lastName": "last",
      "email": "email",
      "contactNumber": "tel"
    },
    "propertyAddress": {
      "addressLine1": "line1",
      "addressLine2": "line2",
      "town": "town",
      "county": "county",
      "postcode": "postcode"
    },
    "isCouncilTaxEnquiry": true,
    "enquiryCategoryMsg": "eq",
    "subEnquiryCategoryMsg": "seq",
    "message": "message"
  }""""

  val wrongJson =
    """{
    "contact": {
      "firstName": "first",
      "lastName": "last",
      "email": "email"
    },
    "propertyAddress": {
      "county": "county",
      "postcode": "postcode"
    },
    "subEnquiryCategoryMsg": "seq",
    "message": "message"
  }""""

  val confirmedContactDetails = ConfirmedContactDetails("a", "b", "c", "d")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "postcode")
  val mockConnector = mock[EmailConnector]
  when(mockConnector.sendEmail(any[Email])) thenReturn Future.successful(Try(202))
  val mockConnectorFailed = mock[EmailConnector]
  when(mockConnectorFailed.sendEmail(any[Email])) thenReturn Future.successful(Failure(new RuntimeException("Received exception from upstream service")))
  val init = injector.instanceOf[Initialize]

  "Given some Json representing a Contact with an enquiry, the createContact method creates a Right(Contact) with council tax address details" in {
    val controller = new CreationController(mockConnector, init)
    val result = controller.createContact(Some(Json.parse(contactJson)))

    result.isRight mustBe true
    result.right.get.contact mustBe ConfirmedContactDetails("first", "last", "email", "tel")
    result.right.get.propertyAddress mustBe PropertyAddress("line1", Some("line2"), "town", Some("county"), "postcode")
    result.right.get.enquiryCategoryMsg mustBe "eq"
    result.right.get.subEnquiryCategoryMsg mustBe "seq"
    result.right.get.message mustBe "message"
  }

  "return 200 for a POST carrying an enquiry" in {
    val result = new CreationController(mockConnector, init).create()(fakeRequestWithJson(contactJson))
    status(result) mustBe OK
  }

  "return 400 (badrequest) when given no json" in {
    val fakeRequest = FakeRequest("POST", "").withHeaders("Content-Type" -> "application/json")
    val result = new CreationController(mockConnector, init).create()(fakeRequest)
    status(result) mustBe BAD_REQUEST
  }

  "return 400 (badrequest) when given garbled json" in {
    val fakeRequest = FakeRequest("POST", "").withHeaders("Content-Type" -> "application/json").withTextBody("{")
    val result = new CreationController(mockConnector, init).create()(fakeRequest)
    status(result) mustBe BAD_REQUEST
  }

  "Given some wrong Json format, the createContact method returns a Left(Unable to parse)" in {
    val controller = new CreationController(mockConnector, init)
    val result = controller.createContact(Some(Json.parse(wrongJson)))

    result.isLeft mustBe true
  }

  "Create method returns a Failure when the email service returns an internal server error" in {
    intercept[Exception] {
      val result = new CreationController(mockConnectorFailed, init).create()(fakeRequestWithJson(contactJson))
      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }
}
