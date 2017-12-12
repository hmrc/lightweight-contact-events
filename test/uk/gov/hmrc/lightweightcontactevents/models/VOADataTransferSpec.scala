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

/*
{
"version": 0,
"timestamp": "2017-12-01T14:23:10+00:00",
"domain": "CT"
"categories": ["Council Tax", "My property is in poor repair or
uninhabitable"],
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
}
 */

class VOADataTransferSpec extends SpecBase {
  val message = "MSG"
  val enquiryCategoryMsg = "Council Tax"
  val subEnquiryCategoryMsg = "My property is in poor repair or uninhabitable"
  val confirmedContactDetails = ConfirmedContactDetails("first", "last", "email", "07777777")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val contact = Contact(confirmedContactDetails, propertyAddress, true, enquiryCategoryMsg, subEnquiryCategoryMsg, message)

  "creating an VOADataTransfer object from a contact results in a map of parameters containing a version key set to a value of 0" in {
    VOADataTransfer(contact).version mustBe 0
  }

  "creating an VOADataTransfer object from a contact results in a case class containing a timestamp key where the time is approximately the current time" in {}

  "creating an VOADataTransfer object from a contact results in a case class containing a domain key set to CT if this is a contact relating to council tax" in {}

  "creating an VOADataTransfer object from a contact results in a case class containing a domain key set to NDR if this is a contact relating to business rates" in {}

  "creating an VOADataTransfer object from a contact results in a case class containing a categories list with the enquiryCategoryMsg and subEnquiryCategoryMsg" in {}

  "creating an VOADataTransfer object from a contact results in a case class containing a firstName equal to the firstName in the contact" in {}

  "creating an VOADataTransfer object from a contact results in a case class containing a lastName equal to the lastName in the contact" in {}

  "creating an VOADataTransfer object from a contact results in a case class containing an email address equal to the emailAddress in the contact" in {}

  "creating an VOADataTransfer object from a contact results in a case class containing a phone equal to the phone in the contact address" in {}

  "creating an VOADataTransfer object from a contact results in a case class containing a property address equal to the property address in the contact" in {}

  "creating an VOADataTransfer object from a contact results in a case class containing a message equal to the message in the contact" in {}
  
}
