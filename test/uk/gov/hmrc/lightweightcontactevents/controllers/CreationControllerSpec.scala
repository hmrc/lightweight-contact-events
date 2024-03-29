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

package uk.gov.hmrc.lightweightcontactevents.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatestplus.mockito.MockitoSugar
import play.api.{Configuration, Environment}
import play.api.libs.json.Json
import play.api.mvc.DefaultActionBuilder
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import uk.gov.hmrc.lightweightcontactevents.connectors.AuditingService
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, PropertyAddress, QueuedDataTransfer}
import uk.gov.hmrc.lightweightcontactevents.repository.QueuedDataTransferRepository
import uk.gov.hmrc.lightweightcontactevents.utils.Initialize

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.{AnyContentAsJson, ControllerComponents}

class CreationControllerSpec extends SpecBase with MockitoSugar with EitherValues {

  implicit val ec: ExecutionContext = injector.instanceOf[ExecutionContext]
  val configuration: Configuration  = injector.instanceOf[Configuration]
  val environment: Environment      = injector.instanceOf[Environment]
  val auditService: AuditingService = injector.instanceOf[AuditingService]
  val initialize: Initialize        = injector.instanceOf[Initialize]
  val action: DefaultActionBuilder  = injector.instanceOf(classOf[DefaultActionBuilder])

  val stub: ControllerComponents = Helpers.stubControllerComponents()

  def fakeRequestWithJson(jsonStr: String): FakeRequest[AnyContentAsJson] = {
    val json = Json.parse(jsonStr)
    FakeRequest("POST", "").withHeaders("Content-Type" -> "application/json").withJsonBody(json)
  }

  val contactJson =
    """{
    "contact": {
      "fullName": "full name",
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
    "contactReason": "more_details",
    "enquiryCategoryMsg": "Council Tax",
    "subEnquiryCategoryMsg": "seq",
    "message": "message"
  }""""

  val brContactJson =
    """{
    "contact": {
      "fullName": "full name",
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
    "isCouncilTaxEnquiry": false,
    "contactReason": "more_details",
    "enquiryCategoryMsg": "Business rates",
    "subEnquiryCategoryMsg": "seq",
    "message": "message"
  }""""

  val haContactJson =
    """{
    "contact": {
      "fullName": "full name",
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
    "isCouncilTaxEnquiry": false,
    "contactReason": "more_details",
    "enquiryCategoryMsg": "Housing Benefit and Local Housing Allowances",
    "subEnquiryCategoryMsg": "seq",
    "message": "message"
  }""""

  val frContactJson =
    """{
    "contact": {
      "fullName": "full name",
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
    "isCouncilTaxEnquiry": false,
    "contactReason": "more_details",
    "enquiryCategoryMsg": "Fair rents",
    "subEnquiryCategoryMsg": "seq",
    "message": "message"
  }""""

  val oContactJson =
    """{
    "contact": {
      "fullName": "full name",
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
    "isCouncilTaxEnquiry": false,
    "contactReason": "more_details",
    "enquiryCategoryMsg": "Other",
    "subEnquiryCategoryMsg": "seq",
    "message": "message"
  }""""

  val wrongEnquiryCategoryJson =
    """{
    "contact": {
      "fullName": "full name",
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
    "isCouncilTaxEnquiry": false,
    "contactReason": "more_details",
    "enquiryCategoryMsg": "other",
    "subEnquiryCategoryMsg": "seq",
    "message": "message"
  }""""

  val wrongJson =
    """{
    "contact": {
      "fullName": "full name",
      "email": "email"
    },
    "propertyAddress": {
      "county": "county",
      "postcode": "postcode"
    },
    "subEnquiryCategoryMsg": "seq",
    "message": "message"
  }""""

  "Given some Json representing a Contact with an enquiry, the createContact method creates a Right(Contact) with council tax address details" in {
    val repository = getQueuedDataTransferRepository()
    val controller = new CreationController(repository, initialize, action, stub)
    val result     = controller.createContact(Some(Json.parse(contactJson)))

    result.isRight mustBe true
    result.value.contact mustBe ConfirmedContactDetails("full name", "email", "tel")
    result.value.propertyAddress mustBe PropertyAddress("line1", Some("line2"), "town", Some("county"), "postcode")
    result.value.enquiryCategoryMsg mustBe "Council Tax"
    result.value.subEnquiryCategoryMsg mustBe "seq"
    result.value.message mustBe "message"
  }

  "return 200 for a POST carrying an enquiry for council tax" in {
    val repository = getQueuedDataTransferRepository()

    val result = new CreationController(repository, initialize, action, stub).create()(fakeRequestWithJson(contactJson))
    status(result) mustBe OK
  }

  "return 200 for a POST carrying an enquiry for business rates" in {
    val repository = getQueuedDataTransferRepository()

    val result = new CreationController(repository, initialize, action, stub).create()(fakeRequestWithJson(brContactJson))
    status(result) mustBe OK
  }

  "return 200 for a POST carrying an enquiry for housing allowance" in {
    val repository = getQueuedDataTransferRepository()

    val result = new CreationController(repository, initialize, action, stub).create()(fakeRequestWithJson(haContactJson))
    status(result) mustBe OK
  }

  "return 200 for a POST carrying an enquiry for fair rent" in {
    val repository = getQueuedDataTransferRepository()

    val result = new CreationController(repository, initialize, action, stub).create()(fakeRequestWithJson(frContactJson))
    status(result) mustBe OK
  }

  "return 200 for a POST carrying an enquiry for other" in {
    val repository = getQueuedDataTransferRepository()

    val result = new CreationController(repository, initialize, action, stub).create()(fakeRequestWithJson(oContactJson))
    status(result) mustBe OK
  }

  "return 400 (badrequest) when given no json" in {
    val fakeRequest = FakeRequest("POST", "").withHeaders("Content-Type" -> "application/json")
    val repository  = getQueuedDataTransferRepository()

    val result = new CreationController(repository, initialize, action, stub).create()(fakeRequest)
    status(result) mustBe BAD_REQUEST
  }

  "return 400 (badrequest) when given garbled json" in {
    val fakeRequest = FakeRequest("POST", "").withHeaders("Content-Type" -> "application/json").withTextBody("{")
    val repository  = getQueuedDataTransferRepository()

    val result = new CreationController(repository, initialize, action, stub).create()(fakeRequest)
    status(result) mustBe BAD_REQUEST
  }

  "return 500 (internal server errro) when repository is unable to enqueue request " in {
    val repositoryMock = mock[QueuedDataTransferRepository]
    when(repositoryMock.insert(any[QueuedDataTransfer])).thenReturn(Future.failed(new Exception("Unable to store")))

    val result = new CreationController(repositoryMock, initialize, action, stub).create()(fakeRequestWithJson(contactJson))
    status(result) mustBe INTERNAL_SERVER_ERROR

  }

  "Given some wrong Json format, the createContact method returns a Left(Unable to parse)" in {
    val repository = getQueuedDataTransferRepository()

    val controller = new CreationController(repository, initialize, action, stub)
    val result     = controller.createContact(Some(Json.parse(wrongJson)))

    result.isLeft mustBe true
  }

  "Create method returns a Failure when the email service returns an internal server error" in {
    intercept[Exception] {
      val repository = getQueuedDataTransferRepository()

      val result = new CreationController(repository, initialize, action, stub).create()(fakeRequestWithJson(contactJson))
      status(result) mustBe INTERNAL_SERVER_ERROR
    }
  }
}
