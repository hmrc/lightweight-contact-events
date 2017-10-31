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

package uk.gov.hmrc.lightweightcontactevents.models

import uk.gov.hmrc.lightweightcontactevents.SpecBase

class EmailSpec extends SpecBase {
  val message = "MSG"
  val enquiryCategory = "EC"
  val subEnquiryCategory = "SEC"
  val confirmedContactDetails = ConfirmedContactDetails("first", "last", "email", "07777777")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val contact = Contact(confirmedContactDetails, propertyAddress, enquiryCategory, subEnquiryCategory, message)

  "creating an email from a contact results in a map of parameters containing a firstName key set to a value of first" in {
    Email(contact).parameters.getOrElse("firstName", "") mustBe "first"
  }

  "creating an email from a contact results in a map of parameters containing a lastName key set to a value of last" in {
    Email(contact).parameters.getOrElse("lastName", "") mustBe "last"
  }

  "creating an email from a contact results in a map of parameters containing a email key set to a value of email" in {
    Email(contact).parameters.getOrElse("email", "") mustBe "email"
  }

  "creating an email from a contact results in a map of parameters containing a contactNumber key set to a value of 07777777" in {
    Email(contact).parameters.getOrElse("contactNumber", "") mustBe "07777777"
  }

  "creating an email from a contact results in a map of parameters containing a addressLine1 key set to a value of line1" in {
    Email(contact).parameters.getOrElse("addressLine1", "") mustBe "line1"
  }

  "creating an email from a contact with an addressLine2 results in a map of parameters containing a addressLine2 key set to a value of line2" in {
    Email(contact).parameters.getOrElse("addressLine2", "") mustBe "line2"
  }

  "creating an email from a contact without an addressLine2 results in a map of parameters without an addressLine2 key" in {
    val pa = propertyAddress copy (addressLine2 = None)
    val contact = Contact(confirmedContactDetails, pa, enquiryCategory, subEnquiryCategory, message)
    Email(contact).parameters.isDefinedAt("addressLine2") mustBe false
  }

  "creating an email from a contact with an town results in a map of parameters containing a addressLine2 key set to a value of town" in {
    Email(contact).parameters.getOrElse("town", "") mustBe "town"
  }

  "creating an email from a contact with an county results in a map of parameters containing a county key set to a value of county" in {
    Email(contact).parameters.getOrElse("county", "") mustBe "county"
  }

  "creating an email from a contact without an county results in a map of parameters without county key" in {
    val pa  = propertyAddress copy (county = None)
    val contact = Contact(confirmedContactDetails, pa, enquiryCategory, subEnquiryCategory, message)
    Email(contact).parameters.isDefinedAt("county") mustBe false
  }

  "creating an email from a contact with an postcode results in a map of parameters containing a postcode key set to a value of AA1 1AA" in {
    Email(contact).parameters.getOrElse("postcode", "") mustBe "AA1 1AA"
  }

  "creating an email from a contact with an enquiry category results in a map of parameters containing a enquiryCategory key set to a value of EC" in {
    Email(contact).parameters.getOrElse("enquiryCategoryMsg", "") mustBe "EC"
  }

  "creating an email from a contact with an sub enquiry category results in a map of parameters containing a subEnquiryCategory key set to a value of SC" in {
    Email(contact).parameters.getOrElse("subEnquiryCategoryMsg", "") mustBe "SEC"
  }

  "creating an email from a contact with an message results in a map of parameters containing a message key set to a value of MSG" in {
    Email(contact).parameters.getOrElse("message", "") mustBe "MSG"
  }
}