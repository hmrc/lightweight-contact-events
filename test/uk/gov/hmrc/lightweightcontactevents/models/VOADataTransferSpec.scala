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

package uk.gov.hmrc.lightweightcontactevents.models

import uk.gov.hmrc.lightweightcontactevents.SpecBase

class VOADataTransferSpec extends SpecBase {
  val message = "MSG"
  val enquiryCategoryMsg = "Council Tax"
  val subEnquiryCategoryMsg = "My property is in poor repair or uninhabitable"
  val confirmedContactDetails = ConfirmedContactDetails("first", "last", "email", "07777777")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val ctContact = Contact(confirmedContactDetails, propertyAddress, true, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val ndrContact = Contact(confirmedContactDetails, propertyAddress, false, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val ctDataTransfer = VOADataTransfer(ctContact)

  "creating an VOADataTransfer object from a contact results in a map of parameters containing a version key set to a value of 0" in {
    ctDataTransfer.version mustBe 0
  }

  "creating an VOADataTransfer object from a contact results in a case class containing a timestamp key where the time is approximately the current time" in {
    val timestamp = ctDataTransfer.timestamp
    val creationTime = java.time.LocalDateTime.parse(timestamp)
    val between = java.time.LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.UTC) - creationTime.toEpochSecond(java.time.ZoneOffset.UTC)
    between < 100 mustBe true
  }

  "creating an VOADataTransfer object from a contact results in a case class containing a domain key set to CT if this is a contact relating to council tax" in {
    ctDataTransfer.domain mustBe "CT"
  }

  "creating an VOADataTransfer object from a contact results in a case class containing a domain key set to NDR if this is a contact relating to business rates" in {
    VOADataTransfer(ndrContact).domain mustBe "NDR"
  }

  "creating an VOADataTransfer object from a contact results in a case class containing a categories list with the enquiryCategoryMsg and subEnquiryCategoryMsg" in {
    val cats = ctDataTransfer.categories
    cats.size mustBe 2
    cats(0) mustBe enquiryCategoryMsg
    cats(1) mustBe subEnquiryCategoryMsg
  }

  "creating an VOADataTransfer object from a contact results in a case class containing a firstName equal to the firstName in the contact" in {
    ctDataTransfer.firstName mustBe ctContact.contact.firstName
  }

  "creating an VOADataTransfer object from a contact results in a case class containing a lastName equal to the lastName in the contact" in {
    ctDataTransfer.lastName mustBe ctContact.contact.lastName
  }

  "creating an VOADataTransfer object from a contact results in a case class containing an email address equal to the emailAddress in the contact" in {
    ctDataTransfer.email mustBe ctContact.contact.email
  }

  "creating an VOADataTransfer object from a contact results in a case class containing a phone equal to the phone in the contact address" in {
    ctDataTransfer.phone mustBe ctContact.contact.contactNumber

  }

  "creating an VOADataTransfer object from a contact results in a case class containing a property address equal to the property address in the contact" in {
    ctDataTransfer.propertyAddress mustBe propertyAddress
  }

  "creating an VOADataTransfer object from a contact results in a case class containing a message equal to the message in the contact" in {
    ctDataTransfer.message mustBe ctContact.message
  }

}
