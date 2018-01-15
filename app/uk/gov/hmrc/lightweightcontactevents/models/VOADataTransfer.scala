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

import java.time.LocalDateTime

import play.api.libs.json.Json

case class VOADataTransfer(version: Int,
                           timestamp: String,
                           domain: String,
                           categories: Seq[String],
                           firstName: String,
                           lastName: String,
                           email: String,
                           phone: String,
                           propertyAddress: PropertyAddress,
                           message: String)

/**/

object VOADataTransfer {
  implicit val writer = Json.writes[VOADataTransfer]

  def apply(ctc: Contact): VOADataTransfer =
    VOADataTransfer(0,
      LocalDateTime.now().toString,
      if (ctc.isCouncilTaxEnquiry) "CT" else "NDR",
      Seq(ctc.enquiryCategoryMsg, ctc.subEnquiryCategoryMsg),
      ctc.contact.firstName,
      ctc.contact.lastName,
      ctc.contact.email,
      ctc.contact.contactNumber,
      ctc.propertyAddress,
      ctc.message)
}
