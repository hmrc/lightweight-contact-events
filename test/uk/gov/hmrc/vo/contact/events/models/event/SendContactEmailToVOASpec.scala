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

package uk.gov.hmrc.vo.contact.events.models.event

import play.api.libs.json.Json
import uk.gov.hmrc.vo.contact.events.models.{ConfirmedContactDetailsLegacy, PropertyAddress}
import uk.gov.hmrc.vo.unit.test.{BaseSpec, JsonAssertions}

/**
  * @author Yuriy Tumakha
  */
class SendContactEmailToVOASpec extends BaseSpec with JsonAssertions("event"):

  "SendContactEmailToVOA" should {
    "serialize to expected JSON" in {
      val event     = SendContactEmailToVOA(
        contact = ConfirmedContactDetailsLegacy("First name", "Last name", "user@email.com", "1234567890"),
        propertyAddress = PropertyAddress("address line 1", Some("address line 2"), "Town", Some("County"), "postcode"),
        isCouncilTaxEnquiry = true,
        subject = "Email subject",
        recipientEmailAddress = "recipient.email@test.com",
        enquiryCategoryMsg = "Enquiry category",
        subEnquiryCategoryMsg = "Sub category",
        message = "Message content"
      )
      val eventJson = Json.toJson(event)

      event.auditType shouldBe "SendContactEmailToVOA"

      compareJson(eventJson, "SendContactEmailToVOA.json")
    }
  }

  "SendContactEmailToVOA with minimal data" should {
    "serialize to expected JSON" in {
      val event     = SendContactEmailToVOA(
        contact = ConfirmedContactDetailsLegacy("First name", "Last name", "user@email.com", "1234567890"),
        propertyAddress = PropertyAddress("address line 1", None, "Town", None, "postcode"),
        isCouncilTaxEnquiry = false,
        subject = "Email subject",
        recipientEmailAddress = "recipient.email@test.com",
        enquiryCategoryMsg = "Enquiry category",
        subEnquiryCategoryMsg = "Sub category",
        message = "Message content"
      )
      val eventJson = Json.toJson(event)

      event.auditType shouldBe "SendContactEmailToVOA"

      compareJson(eventJson, "SendContactEmailToVOA_minimal.json")
    }
  }
