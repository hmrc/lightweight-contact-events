/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.lightweightcontactevents.models

import org.mockito.Matchers.anyString
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import uk.gov.hmrc.lightweightcontactevents.utils.{Initialize, LightweightFixture}
import uk.gov.hmrc.lightweightcontactevents.utils.LightweightFixture.{message, _}

class VOADataTransferSpec extends SpecBase with MockitoSugar {

  /* VOADataTransfer Contact Tests */

  "creating a contact case class containing contact details set to confirmed contact details" in {
    ctContact.contact mustBe confirmedContactDetails
  }

  "creating a contact case class containing property address set to confirmed property address" in {
    ctContact.propertyAddress mustBe propertyAddress
  }

  "creating a contact case class containing a isCouncilTaxEnquiry boolean set to true" in {
    ctContact.isCouncilTaxEnquiry mustBe true
  }

  "creating a contact case class containing a isCouncilTaxEnquiry boolean set to false" in {
    brContact.isCouncilTaxEnquiry mustBe false
  }

  "creating a contact case class containing a enquiryCategoryMsg string set to enquiryCategoryMsg" in {
    ctContact.enquiryCategoryMsg mustBe enquiryCategoryMsg
  }

  "creating a contact case class containing a subEnquiryCategoryMsg string set to subEnquiryCategoryMsg" in {
    ctContact.subEnquiryCategoryMsg mustBe subEnquiryCategoryMsg
  }

  "creating a contact case class containing a message string set to message" in {
    ctContact.message mustBe LightweightFixture.message
  }

  /* VOADataTransfer Tests */

  "creating an VOADataTransfer object from values containing a contact details equal to the contact details" in {
    ctDataTransfer.contact mustBe confirmedContactDetails
  }

  "creating an VOADataTransfer object from values containing a property address equal to the property address" in {
    ctDataTransfer.propertyAddress mustBe propertyAddress
  }

  "creating an VOADataTransfer object from values containing a isCouncilTaxEnquiry equal true" in {
    ctDataTransfer.isCouncilTaxEnquiry mustBe true
  }

  "creating an VOADataTransfer object from values containing a isCouncilTaxEnquiry equal false" in {
    brDataTransfer.isCouncilTaxEnquiry mustBe false
  }

  "creating an VOADataTransfer object from values containing a subject equal subject" in {
    ctDataTransfer.subject mustBe subject
  }

  "creating an VOADataTransfer object from values containing a recipientEmailAddress equal ctEmail" in {
    ctDataTransfer.recipientEmailAddress mustBe ctEmail
  }

  "creating an VOADataTransfer object from values containing a recipientEmailAddress equal ndrEmail" in {
    brDataTransfer.recipientEmailAddress mustBe brEmail
  }

  "creating an VOADataTransfer object from values containing a enquiryCategoryMsg equal enquiryCategoryMsg" in {
    ctDataTransfer.enquiryCategoryMsg mustBe enquiryCategoryMsg
  }

  "creating an VOADataTransfer object from values containing a subEnquiryCategoryMsg equal subEnquiryCategoryMsg" in {
    brDataTransfer.subEnquiryCategoryMsg mustBe subEnquiryCategoryMsg
  }

  "creating an VOADataTransfer object from values containing a message equal message" in {
    ctDataTransfer.message mustBe ctContact.message
  }

  "should return an exception when VOADataTransfer object contains a wrong enquiry category" in {
    val init = mock[Initialize]

    intercept[RuntimeException] {
      val wrongDataTransfer = VOADataTransfer(wrongContact, init)
    }
  }
}
