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

import com.typesafe.config.ConfigFactory
import play.api.Configuration
import uk.gov.hmrc.lightweightcontactevents.SpecBase
import uk.gov.hmrc.lightweightcontactevents.utils.Initialize
import scala.collection.JavaConversions._

class EmailSpec extends SpecBase {
  val init = injector.instanceOf[Initialize]
  val message = "MSG"
  val enquiryCategoryMsg = "Council Tax"
  val subEnquiryCategoryMsg = "SEC"
  val confirmedContactDetails = ConfirmedContactDetails("first", "last", "email", "07777777")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val contact = Contact(confirmedContactDetails, propertyAddress, true, enquiryCategoryMsg, subEnquiryCategoryMsg, message)

  "creating an email from a contact results in a map of parameters containing a firstName key set to a value of first" in {
    Email(contact, init).parameters.getOrElse("firstName", "") mustBe "first"
  }

  "creating an email from a contact results in a map of parameters containing a lastName key set to a value of last" in {
    Email(contact, init).parameters.getOrElse("lastName", "") mustBe "last"
  }

  "creating an email from a contact results in a map of parameters containing a email key set to a value of email" in {
    Email(contact, init).parameters.getOrElse("email", "") mustBe "email"
  }

  "creating an email from a contact results in a map of parameters containing a contactNumber key set to a value of 07777777" in {
    Email(contact, init).parameters.getOrElse("contactNumber", "") mustBe "07777777"
  }

  "creating an email from a contact results in a map of parameters containing a propertyAddress key with address values seperated by <br/>" in {
    Email(contact, init).parameters.getOrElse("propertyAddress", "") mustBe "line1<br/>line2<br/>town<br/>county<br/>AA1 1AA"
  }

  "creating an email from a contact with an enquiry category results in a map of parameters containing a enquiryCategory key set to a value of EC" in {
    Email(contact, init).parameters.getOrElse("enquiryCategoryMsg", "") mustBe "Council Tax"
  }

  "creating an email from a contact with an sub enquiry category results in a map of parameters containing a subEnquiryCategory key set to a value of SC" in {
    Email(contact, init).parameters.getOrElse("subEnquiryCategoryMsg", "") mustBe "SEC"
  }

  "creating an email from a contact with an message results in a map of parameters containing a message key set to a value of MSG" in {
    Email(contact, init).parameters.getOrElse("message", "") mustBe "MSG"
  }

  "use the council tax email from initialize if the enquiryCategory is council_tax" in {
    val sm = Map("email.council-tax" -> "ct@voa.gov.uk", "email.non-domestic-rates" -> "ndr@voa.gov.uk")
    val conf = new Configuration(ConfigFactory.parseMap(sm))
    val init = new Initialize(conf)
    Email(contact, init).to(0) mustBe "ct@voa.gov.uk"

  }

  "use the business rates email from initialize if the enquiryCategory is business_rates" in {
    val contact = Contact(confirmedContactDetails, propertyAddress, false, "Business Rates", subEnquiryCategoryMsg, message)
    val sm = Map("email.council-tax" -> "ct@voa.gov.uk", "email.non-domestic-rates" -> "ndr@voa.gov.uk")
    val conf = new Configuration(ConfigFactory.parseMap(sm))
    val init = new Initialize(conf)
    Email(contact, init).to(0) mustBe "ndr@voa.gov.uk"
  }
}
