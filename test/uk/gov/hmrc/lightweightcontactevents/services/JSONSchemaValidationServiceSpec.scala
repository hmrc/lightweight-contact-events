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

import play.api.libs.json._
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import uk.gov.hmrc.lightweightcontactevents.services.JSONSchemaValidationService


class JSONSchemaValidationServiceSpec extends SpecBase {

  val fakeConfiguration = fakeApplication.configuration

  val service = new JSONSchemaValidationService(fakeConfiguration)

  val validJSON = Json.parse("""{
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
  }""")


  implicit class JsValueOps(val jsValue: JsValue) {

    def replace[A <: JsValue](field: String, newValue: A): JsValue =
      jsValue.as[JsObject] ++ Json.obj(field → newValue)

    def replaceInner[A <: JsValue](field: String, innerField: String, newValue: A): JsValue = {
      val inner = (jsValue \ field).getOrElse(sys.error(s"Could not find field $field"))
      val innerReplaced = inner.replace(innerField, newValue)
      jsValue.replace(field, innerReplaced)
    }

    def remove(field: String): JsValue =
      jsValue.as[JsObject] - field

    def removeInner(field: String, innerField: String): JsValue = {
      val inner = (jsValue \ field).getOrElse(sys.error(s"Could not find field $field"))
      val innerReplaced = inner.remove(innerField)
      jsValue.replace(field, innerReplaced)
    }

  }

  "The JSONSchemaValidationService" must {

    def testError(contact: JsValue): Unit =
      service.validateAgainstSchema(Some(contact)).isLeft mustBe true

    "If the outgoing-json validation feature detects no errors return a right" in {
      service.validateAgainstSchema(Some(validJSON)) mustBe Right(validJSON)
    }

    "If the outgoing-json validation feature detects no Json is available then return a Left with no json available message" in {
      service.validateAgainstSchema(None) mustBe Left("No Json available")
    }

    "when given a Contact that the json validation schema reports that the version is the wrong type, return a message" in {
      testError(validJSON.replace("version", JsString("0")))
    }

    "when given a Contact that the json validation schema reports that the version is missing" in {
      testError(validJSON.remove("version"))
    }

    "when given a Contact that the json validation schema reports that the timestamp is the wrong type, return a message" in {
      testError(validJSON.replace("timestamp", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the timestamp is missing" in {
      testError(validJSON.remove("timestamp"))
    }

    "when given a Contact that the json validation schema reports that the categories is the wrong type, return a message" in {
      testError(validJSON.replace("categories", JsArray(Seq(JsNumber(0), JsNumber(1)))))
    }

    "when given a Contact that the json validation schema reports that the categories is missing" in {
      testError(validJSON.remove("categories"))
    }

    "when given a Contact that the json validation schema reports that the first-name is the wrong type, return a message" in {
      testError(validJSON.replace("first-name", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the first-name is missing" in {
      testError(validJSON.remove("first-name"))
    }

    "when given a Contact that the json validation schema reports that the last-name is the wrong type, return a message" in {
      testError(validJSON.replace("last-name", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the last-name is missing" in {
      testError(validJSON.remove("last-name"))
    }

    "when given a Contact that the json validation schema reports that the email is the wrong type, return a message" in {
      testError(validJSON.replace("email", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the email is missing" in {
      testError(validJSON.remove("email"))
    }

    "when given a Contact that the json validation schema reports that the phone is the wrong type, return a message" in {
      testError(validJSON.replace("phone", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the phone is missing" in {
      testError(validJSON.remove("phone"))
    }

    "when given a Contact that the json validation schema reports that the line1 is the wrong type, return a message" in {
      testError(validJSON.replace("line1", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the line1 is missing" in {
      testError(validJSON.removeInner("property-address", "line1"))
    }

    "when given a Contact that the json validation schema reports that the town is the wrong type, return a message" in {
      testError(validJSON.replace("town", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the town is missing" in {
      testError(validJSON.removeInner("property-address", "town"))
    }

    "when given a Contact that the json validation schema reports that the postcode is the wrong type, return a message" in {
      testError(validJSON.replace("postcode", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the postcode is missing" in {
      testError(validJSON.removeInner("property-address", "postcode"))
    }

    "when given a Contact that the json validation schema reports that the message is the wrong type, return a message" in {
      testError(validJSON.replace("message", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the message is missing" in {
      testError(validJSON.remove("message"))
    }

    "when given a Contact that the json validation schema reports that the line2 is the wrong type, return a message" in {
      testError(validJSON.replace("line2", JsNumber(0)))
    }

    "when given a Contact that the json validation schema reports that the county is the wrong type, return a message" in {
      testError(validJSON.replace("county", JsNumber(0)))
    }

  }
}
