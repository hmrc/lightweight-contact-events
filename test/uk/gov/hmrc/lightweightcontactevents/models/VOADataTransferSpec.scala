/*
 * Copyright 2020 HM Revenue & Customs
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

import uk.gov.hmrc.lightweightcontactevents.SpecBase

class VOADataTransferSpec extends SpecBase {
  val message = "MSG"
  val subject = "Valuation Office Agency Contact Form"
  val ctEmail = "ct.email@voa.gsi.gov.uk"
  val ndrEmail = "ndr.email@voa.gsi.gov.uk"
  val enquiryCategoryMsg = "Council Tax"
  val subEnquiryCategoryMsg = "My property is in poor repair or uninhabitable"
  val confirmedContactDetails = ConfirmedContactDetails("first", "last", "email", "07777777")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val ctContact = Contact(confirmedContactDetails, propertyAddress, true, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val ndrContact = Contact(confirmedContactDetails, propertyAddress, false, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val ctDataTransfer = VOADataTransfer(confirmedContactDetails, propertyAddress, true, subject, ctEmail, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val ndrDataTransfer = VOADataTransfer(confirmedContactDetails, propertyAddress, false, subject, ndrEmail, enquiryCategoryMsg, subEnquiryCategoryMsg, message)

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
    ndrContact.isCouncilTaxEnquiry mustBe false
  }

  "creating a contact case class containing a enquiryCategoryMsg string set to enquiryCategoryMsg" in {
    ctContact.enquiryCategoryMsg mustBe enquiryCategoryMsg
  }

  "creating a contact case class containing a subEnquiryCategoryMsg string set to subEnquiryCategoryMsg" in {
    ctContact.subEnquiryCategoryMsg mustBe subEnquiryCategoryMsg
  }

  "creating a contact case class containing a message string set to message" in {
    ctContact.message mustBe message
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
    ndrDataTransfer.isCouncilTaxEnquiry mustBe false
  }

  "creating an VOADataTransfer object from values containing a subject equal subject" in {
    ctDataTransfer.subject mustBe subject
  }

  "creating an VOADataTransfer object from values containing a recipientEmailAddress equal ctEmail" in {
    ctDataTransfer.recipientEmailAddress mustBe ctEmail
  }

  "creating an VOADataTransfer object from values containing a recipientEmailAddress equal ndrEmail" in {
    ndrDataTransfer.recipientEmailAddress mustBe ndrEmail
  }

  "creating an VOADataTransfer object from values containing a enquiryCategoryMsg equal enquiryCategoryMsg" in {
    ctDataTransfer.enquiryCategoryMsg mustBe enquiryCategoryMsg
  }

  "creating an VOADataTransfer object from values containing a subEnquiryCategoryMsg equal subEnquiryCategoryMsg" in {
    ndrDataTransfer.subEnquiryCategoryMsg mustBe subEnquiryCategoryMsg
  }

  "creating an VOADataTransfer object from values containing a message equal message" in {
    ctDataTransfer.message mustBe ctContact.message
  }

}
