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

import java.time.LocalDateTime

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

case class VOADataTransfer(version: Int, timestamp: String, domain: String, categories: Seq[String], firstName: String)

object VOADataTransfer {
  def apply(ctc: Contact): VOADataTransfer =
    VOADataTransfer(0,
      LocalDateTime.now().toString,
      if (ctc.isCouncilTaxEnquiry) "CT" else "NDR",
      Seq(ctc.enquiryCategoryMsg, ctc.subEnquiryCategoryMsg),
      ctc.contact.firstName)
}
