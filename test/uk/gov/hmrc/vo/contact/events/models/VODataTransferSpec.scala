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

package uk.gov.hmrc.vo.contact.events.models

import uk.gov.hmrc.vo.contact.events.utils.LightweightFixture.*
import uk.gov.hmrc.vo.contact.events.utils.{Initialize, LightweightFixture}
import uk.gov.hmrc.vo.unit.test.BaseAppSpec

class VODataTransferSpec extends BaseAppSpec:

  private val init: Initialize = mock[Initialize]

  "VODataTransfer" should {
    "creating a contact case class containing contact details set to confirmed contact details" in {
      ctContact.contact shouldBe confirmedContactDetails
    }

    "creating a contact case class containing property address set to confirmed property address" in {
      ctContact.propertyAddress shouldBe propertyAddress
    }

    "creating a contact case class containing a isCouncilTaxEnquiry boolean set to true" in {
      ctContact.isCouncilTaxEnquiry shouldBe true
    }

    "creating a contact case class containing a isCouncilTaxEnquiry boolean set to false" in {
      brContact.isCouncilTaxEnquiry shouldBe false
    }

    "creating a contact case class containing a contactReason set to more_details" in {
      ctContact.contactReason shouldBe contactReason
    }

    "creating a contact case class containing a enquiryCategoryMsg string set to enquiryCategoryMsg" in {
      ctContact.enquiryCategoryMsg shouldBe enquiryCategoryMsg
    }

    "creating a contact case class containing a subEnquiryCategoryMsg string set to subEnquiryCategoryMsg" in {
      ctContact.subEnquiryCategoryMsg shouldBe subEnquiryCategoryMsg
    }

    "creating a contact case class containing a message string set to message" in {
      ctContact.message shouldBe LightweightFixture.message
    }

    "creating an VODataTransfer object from values containing a contact details equal to the contact details" in {
      ctDataTransfer.contact shouldBe ConfirmedContactDetailsLegacy("full name", lastName = "", "email", "07777777")
    }

    "creating an VODataTransfer object from values containing a property address equal to the property address" in {
      ctDataTransfer.propertyAddress shouldBe propertyAddress
    }

    "creating an VODataTransfer object from values containing a subject equal subject" in {
      ctDataTransfer.subject shouldBe subject
    }

    "creating an VODataTransfer object from values containing a recipientEmailAddress equal ctEmail" in {
      ctDataTransfer.recipientEmailAddress shouldBe ctEmail
    }

    "creating an VODataTransfer object from values containing a recipientEmailAddress equal ndrEmail" in {
      brDataTransfer.recipientEmailAddress shouldBe brEmail
    }

    "creating an VODataTransfer object from values containing a enquiryCategoryMsg equal enquiryCategoryMsg" in {
      ctDataTransfer.enquiryCategoryMsg shouldBe enquiryCategoryMsg
    }

    "creating an VODataTransfer object from values containing a subEnquiryCategoryMsg equal subEnquiryCategoryMsg" in {
      brDataTransfer.subEnquiryCategoryMsg shouldBe subEnquiryCategoryMsg
    }

    "creating an VODataTransfer object from values containing a message equal message" in {
      ctDataTransfer.message shouldBe ctContact.message
    }

    "return an exception when VODataTransfer object contains a wrong enquiry category" in {
      val init = mock[Initialize]

      intercept[RuntimeException] {
        VODataTransfer(wrongContact, init)
      }
    }

    "return the correct subject and email address when the contact reason is equal to 'new_enquiry'" in {
      val contact = brContact.copy(contactReason = "new_enquiry", enquiryCategoryMsg = "Council Tax")
      val subject = "CF My property is in poor repair or uninhabitable AA11AA"
      when(init.subjectText).thenReturn(subject)
      when(init.councilTaxEmail).thenReturn(brEmail)

      val voDataTransfer = VODataTransfer(contact, init)
      voDataTransfer.subject               shouldBe subject
      voDataTransfer.recipientEmailAddress shouldBe brEmail
    }

    "return the correct subject and email address when the contact reason is equal to 'new_enquiry' and the enquiry category is equal to 'Other'" in {
      val contact = brContact.copy(contactReason = "new_enquiry", enquiryCategoryMsg = "Other")
      when(init.subjectText).thenReturn(subject)
      when(init.otherEmail).thenReturn(brEmail)

      val voDataTransfer = VODataTransfer(contact, init)
      voDataTransfer.subject               shouldBe subject
      voDataTransfer.recipientEmailAddress shouldBe brEmail
    }

    "return the correct subject and email address when the contact reason is equal to 'more_details'" in {
      val contact = brContact.copy(contactReason = "more_details", enquiryCategoryMsg = "Council Tax")
      when(init.subjectAddInfo).thenReturn(subject)
      when(init.councilTaxEmail).thenReturn(brEmail)

      val voDataTransfer = VODataTransfer(contact, init)
      voDataTransfer.subject               shouldBe s"$subject $postCode"
      voDataTransfer.recipientEmailAddress shouldBe brEmail
    }

    "return the correct subject and email address when the contact reason is equal to 'more_details' and the enquiry category is equal to 'Other'" in {
      val contact = brContact.copy(contactReason = "more_details", enquiryCategoryMsg = "Other")
      when(init.subjectOtherAddInfo).thenReturn(subject)
      when(init.otherEmail).thenReturn(brEmail)

      val voDataTransfer = VODataTransfer(contact, init)
      voDataTransfer.subject               shouldBe s"$subject $postCode"
      voDataTransfer.recipientEmailAddress shouldBe brEmail
    }

    "return the correct subject and email address when the contact reason is equal to 'update_existing'" in {
      val contact = brContact.copy(contactReason = "update_existing", enquiryCategoryMsg = "Council Tax")
      when(init.subjectChase).thenReturn(subject)
      when(init.councilTaxEmail).thenReturn(brEmail)

      val voDataTransfer = VODataTransfer(contact, init)
      voDataTransfer.subject               shouldBe s"$subject $postCode"
      voDataTransfer.recipientEmailAddress shouldBe brEmail
    }

    "return the correct subject and email address when the contact reason is equal to 'update_existing' and the enquiry category is equal to 'Other'" in {
      val contact = brContact.copy(contactReason = "update_existing", enquiryCategoryMsg = "Other")
      when(init.subjectOtherChase).thenReturn(subject)
      when(init.otherEmail).thenReturn(brEmail)

      val voDataTransfer = VODataTransfer(contact, init)
      voDataTransfer.subject               shouldBe s"$subject $postCode"
      voDataTransfer.recipientEmailAddress shouldBe brEmail
    }

    "return the correct subject and email for contact reason 'new_enquiry' and enquiry category 'Housing Benefit and Local Housing Allowances'" in {
      val initialize     = inject[Initialize]
      val voDataTransfer = VODataTransfer(housingBenefitContact, initialize)

      voDataTransfer.recipientEmailAddress shouldBe housingBenefitEmail
      voDataTransfer.subject               shouldBe s"CF - other - $postCode"
      voDataTransfer                       shouldBe housingBenefitDataTransfer
    }
  }
