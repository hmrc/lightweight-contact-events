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

package uk.gov.hmrc.lightweightcontactevents.controllers


import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import uk.gov.hmrc.lightweightcontactevents.models.{BusinessRatesAddress, ConfirmedContactDetails, CouncilTaxAddress}

class CreationControllerSpec extends SpecBase {

  def fakeRequestWithJson(jsonStr: String) = {
    val json = Json.parse(jsonStr)
    FakeRequest("POST", "").withHeaders("Content-Type" ->  "application/json").withJsonBody(json)
  }

  val councilTaxJson = """{
    "contact": {
      "firstName": "first",
      "lastName": "last",
      "email": "email",
      "telephone": "tel",
      "mobile": "mob",
      "contactPreference": "pref"
    },
    "councilTaxAddress": {
      "addressLine1": "line1",
      "addressLine2": "line2",
      "town": "town",
      "county": "county",
      "postcode": "postcode"
    },
    "enquiryCategory": "eq",
    "subEnquiryCategory": "seq",
    "message": "message"
  }""""

  val businessRatesJson =
    """{
    |"contact": {
    |  "firstName": "first",
    |  "lastName": "last",
    |  "email": "email",
    |  "telephone": "tel",
    |  "mobile": "mob",
    |  "contactPreference": "pref"
    |},
    |"businessRatesAddress": {
    |   "businessName": "name",
    |   "businessAddressLine1": "line1",
    |   "businessAddressLine2": "line2",
    |   "businessAddressLine3": "line3",
    |   "town": "town",
    |   "county": "county",
    |   "postcode": "postcode"
    |},
    |"enquiryCategory": "eq",
    |"subEnquiryCategory": "seq",
    |"message": "message"
  }"""".stripMargin

  val bothJson =
    """{
      |    "contact": {
      |      "firstName": "first",
      |      "lastName": "last",
      |      "email": "email",
      |      "telephone": "tel",
      |      "mobile": "mob",
      |      "contactPreference": "pref"
      |    },
      |    "councilTaxAddress": {
      |      "addressLine1": "line1",
      |      "addressLine2": "line2",
      |      "town": "town",
      |      "county": "county",
      |      "postcode": "postcode"
      |    },
      |    "businessRatesAddress": {
      |       "businessName": "name",
      |       "businessAddressLine1": "line1",
      |       "businessAddressLine2": "line2",
      |       "businessAddressLine3": "line3",
      |       "town": "town",
      |       "county": "county",
      |       "postcode": "postcode"
      |    },
      |    "enquiryCategory": "eq",
      |    "subEnquiryCategory": "seq",
      |    "message": "message"
      |  }
    """.stripMargin

  "Given some Json representing a Contact with a council tax enquiry, the createContact method creates a Right(Contact) with council tax address details" in {
    val controller = new CreationController()
    val result = controller.createContact(Some(Json.parse(councilTaxJson)))

    result.isRight mustBe true
    result.right.get.contact mustBe ConfirmedContactDetails("first", "last", "email", "tel", "mob", "pref")
    result.right.get.councilTaxAddress.isDefined mustBe true
    result.right.get.councilTaxAddress.get mustBe CouncilTaxAddress("line1", "line2", "town", "county", "postcode")
    result.right.get.businessRatesAddress.isDefined mustBe false
    result.right.get.enquiryCategory mustBe "eq"
    result.right.get.subEnquiryCategory mustBe "seq"
    result.right.get.message mustBe "message"
  }

  "Given some Json representing a Contact with a business rates enquiry, the createContact method creates a Right(Contact) with busines rates address  details" in {
    val controller = new CreationController()
    val result = controller.createContact(Some(Json.parse(businessRatesJson)))

    result.isRight mustBe true
    result.right.get.contact mustBe ConfirmedContactDetails("first", "last", "email", "tel", "mob", "pref")
    result.right.get.businessRatesAddress.isDefined mustBe true
    result.right.get.businessRatesAddress.get mustBe BusinessRatesAddress("name", "line1", "line2", "line3", "town", "county", "postcode")
    result.right.get.councilTaxAddress.isDefined mustBe false
    result.right.get.enquiryCategory mustBe "eq"
    result.right.get.subEnquiryCategory mustBe "seq"
    result.right.get.message mustBe "message"
  }

  "given some Json representing a contact, it may only have a council tax address or business rates address" in {
    val controller = new CreationController()
    val result = controller.createContact(Some(Json.parse(bothJson)))

    result.isLeft mustBe true
    result.left.get mustBe "Json contains both council tax address and business rates address"
  }

  "return 200 for a POST carrying council tax enquiry" in {
    val result = new CreationController().create()(fakeRequestWithJson(councilTaxJson))
    status(result) mustBe OK
  }

  "return 200 for a POST carrying a business rate enquiry" in {
    val result = new CreationController().create()(fakeRequestWithJson(businessRatesJson))
    status(result) mustBe OK
  }

  "return 400 (badrequest) when json carrying both a business rate and council tax enquiry" in {
    val result = new CreationController().create()(fakeRequestWithJson(bothJson))
    status(result) mustBe BAD_REQUEST
  }

  "return 400 (badrequest) when given no json" in {
    val fakeRequest = FakeRequest("POST", "").withHeaders("Content-Type" ->  "application/json")
    val result = new CreationController().create()(fakeRequest)
    status(result) mustBe BAD_REQUEST
  }

  "return 400 (badrequest) when given garbled json" in {
    val fakeRequest = FakeRequest("POST", "").withHeaders("Content-Type" ->  "application/json").withTextBody("{")
    val result = new CreationController().create()(fakeRequest)
    status(result) mustBe BAD_REQUEST
  }
}
