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

import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.lightweightcontactevents.utils.Initialize

case class VOADataTransfer(contact: ConfirmedContactDetailsLegacy,
                           propertyAddress: PropertyAddress,
                           isCouncilTaxEnquiry: Boolean,
                           subject: String,
                           recipientEmailAddress: String,
                           enquiryCategoryMsg: String,
                           subEnquiryCategoryMsg: String,
                           message: String)

/**/

object VOADataTransfer {
  implicit val format = Json.format[VOADataTransfer]

  def apply(ctc: Contact, init: Initialize): VOADataTransfer = {
    VOADataTransfer(toLegacyContact(ctc.contact),
      ctc.propertyAddress,
      ctc.isCouncilTaxEnquiry,
      getSubjectText(ctc.contactReason,ctc.enquiryCategoryMsg,
        ctc.propertyAddress.postcode,init),
      getEmailAddress(ctc.enquiryCategoryMsg, init),
      ctc.enquiryCategoryMsg,
      ctc.subEnquiryCategoryMsg,
      ctc.message)
  }

  private def getEmailAddress(enquiryCategoryMsg: String, init: Initialize): String =
    enquiryCategoryMsg match {
      case "Council Tax" => init.councilTaxEmail
      case "Business rates" => init.businessRatesEmail
      case "Housing Allowance or fair rents" => init.housingAllowanceEmail
      case "Other" => init.otherEmail
      case _ =>
        Logger.error(s"Email address not found for enquiryCategory : $enquiryCategoryMsg")
        throw new RuntimeException(s"Email address not found for enquiryCategory : $enquiryCategoryMsg")
    }

  private def getSubjectText(contactReason: String, enquiryCategoryMsg: String, postCode: String, init: Initialize):String = {
    val ucPostCode = postCode.replaceAll("\\s+","").toUpperCase
    (contactReason, enquiryCategoryMsg) match {
      case ("more_details", "Other") => s"${init.subjectOtherAddInfo} $ucPostCode"
      case ("update_existing", "Other") => s"${init.subjectOtherChase} $ucPostCode"
      case ("more_details", _ ) => s"${init.subjectAddInfo} $ucPostCode"
      case ("update_existing", _) => s"${init.subjectChase} $ucPostCode"
      case _ => s"${init.subjectText}"
    }
  }
}
