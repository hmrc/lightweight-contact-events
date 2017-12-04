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

import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.lightweightcontactevents.SpecBase

class ValidationControllerSpec extends SpecBase with MockitoSugar {

  def fakeRequestWithJson(jsonStr: String) = {
    val json = Json.parse(jsonStr)
    FakeRequest("POST", "").withHeaders("Content-Type" -> "application/json").withJsonBody(json)
  }

  val fakeConfiguration = fakeApplication.configuration

  val contactJson =
    """{
    "version": 0,
    "timestamp": "2017-12-01T14:23:10+00:00",
    "domain": "CT",
    "categories": ["Council Tax", "My property"],
    "first-name": "Andy",
    "last-name": "Dwelly",
    "email": "andy.dwelly@digital.hmrc.gov.uk",
    "phone": "07525932507",
    "property-address": {
      "line1": "78a High St",
      "line2": "Ferring",
      "town": "Worthing",
      "county": "West Sussex",
      "postcode": "BN443SS"
    },
    "message": "I think I need a reevaluation. What do I do?"
  }"""

  val invalidJson =
    """{
    "version": "wrong format",
    "timestamp": "2017-12-01T14:23:10+00:00",
    "domain": 1,
    "categories": ["Council Tax", "My property"],
    "first-name": "Andy",
    "last-name": "Dwelly",
    "email": "andy.dwelly@digital.hmrc.gov.uk",
    "phone": "07525932507",
    "property-address": {
      "line1": "78a High St",
      "line2": "Ferring",
      "town": "Worthing",
      "county": "West Sussex",
      "postcode": "BN443SS"
    },
    "message": "I think I need a reevaluation. What do I do?"
  }""""


  "return 200 for a POST carrying an enquiry" in {
    val result = new ValidationController(fakeConfiguration).validate()(fakeRequestWithJson(contactJson))
    status(result) mustBe OK
  }

  "return 400 (badrequest) when given no json" in {
    val fakeRequest = FakeRequest("POST", "").withHeaders("Content-Type" -> "application/json")
    val result = new ValidationController(fakeConfiguration).validate()(fakeRequest)
    status(result) mustBe BAD_REQUEST
  }

  "return 400 (badrequest) when given garbled json" in {
    val fakeRequest = FakeRequest("POST", "").withHeaders("Content-Type" -> "application/json").withTextBody("{")
    val result = new ValidationController(fakeConfiguration).validate()(fakeRequest)
    status(result) mustBe BAD_REQUEST
  }

  "Given some invalid Json format, the validate method returns a Left(error)" in {
    val result = new ValidationController(fakeConfiguration).validate()(fakeRequestWithJson(invalidJson))
    status(result) mustBe BAD_REQUEST
  }
}
