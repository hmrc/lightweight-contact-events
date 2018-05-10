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

import play.api.libs.json.Json
import uk.gov.hmrc.lightweightcontactevents.utils.Initialize

case class VOADataTransfer(contact: ConfirmedContactDetails,
                           propertyAddress: PropertyAddress,
                           isCouncilTaxEnquiry: Boolean,
                           subject: String,
                           recipientEmailAddress: String,
                           enquiryCategoryMsg: String,
                           subEnquiryCategoryMsg: String,
                           message: String)

/**/

object VOADataTransfer {
  implicit val writer = Json.writes[VOADataTransfer]

  def apply(ctc: Contact, init: Initialize): VOADataTransfer =
    VOADataTransfer(ctc.contact,
      ctc.propertyAddress,
      ctc.isCouncilTaxEnquiry,
      init.subjectTxt,
      if (ctc.isCouncilTaxEnquiry) init.councilTaxEmail else init.businessRatesEmail,
      ctc.enquiryCategoryMsg,
      ctc.subEnquiryCategoryMsg,
      ctc.message)
}
