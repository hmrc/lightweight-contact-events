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

import play.api.Logger
import play.api.libs.json.{Json, OFormat}
import ConfirmedContactDetails.toLegacyContact
import uk.gov.hmrc.vo.contact.events.config.AppConfig

case class VODataTransfer(
  contact: ConfirmedContactDetailsLegacy,
  propertyAddress: PropertyAddress,
  isCouncilTaxEnquiry: Boolean,
  subject: String,
  recipientEmailAddress: String,
  enquiryCategoryMsg: String,
  subEnquiryCategoryMsg: String,
  message: String
)

object VODataTransfer:

  implicit val format: OFormat[VODataTransfer] = Json.format

  def apply(ctc: Contact, appConfig: AppConfig): VODataTransfer =
    VODataTransfer(
      toLegacyContact(ctc.contact),
      ctc.propertyAddress,
      ctc.isCouncilTaxEnquiry,
      getSubjectText(ctc.contactReason, ctc.enquiryCategoryMsg, ctc.subEnquiryCategoryMsg, ctc.propertyAddress.postcode, appConfig),
      getEmailAddress(ctc.enquiryCategoryMsg, appConfig),
      ctc.enquiryCategoryMsg,
      ctc.subEnquiryCategoryMsg,
      ctc.message
    )

  private def getEmailAddress(enquiryCategoryMsg: String, appConfig: AppConfig): String =
    enquiryCategoryMsg match
      case "Council Tax"                                  => appConfig.councilTaxEmail
      case "Business rates"                               => appConfig.businessRatesEmail
      case "Housing Benefit and Local Housing Allowances" => appConfig.housingAllowanceEmail
      case "Fair rents"                                   => appConfig.housingAllowanceEmail
      case "Other"                                        => appConfig.otherEmail
      case _                                              =>
        Logger(getClass).error(s"Email address not found for enquiryCategory : $enquiryCategoryMsg")
        throw RuntimeException(s"Email address not found for enquiryCategory : $enquiryCategoryMsg")

  private def getSubjectText(contactReason: String, enquiryCategoryMsg: String, subEnquiryCategoryMsg: String, postCode: String, appConfig: AppConfig): String =
    val ucPostCode = postCode.replaceAll("\\s+", "").toUpperCase
    (contactReason, enquiryCategoryMsg) match
      case ("more_details", "Other")                                       => s"${appConfig.subjectOtherAddInfo} $ucPostCode"
      case ("update_existing", "Other")                                    => s"${appConfig.subjectOtherChase} $ucPostCode"
      case ("more_details", _)                                             => s"${appConfig.subjectAddInfo} $ucPostCode"
      case ("update_existing", _)                                          => s"${appConfig.subjectChase} $ucPostCode"
      case ("new_enquiry", "Council Tax")                                  => s"CF $subEnquiryCategoryMsg $ucPostCode"
      case ("new_enquiry", "Business Rates")                               => s"CF $subEnquiryCategoryMsg $ucPostCode"
      case ("new_enquiry", "Housing Benefit and Local Housing Allowances") => s"CF - other - $ucPostCode"
      case _                                                               => s"${appConfig.subjectText}"
