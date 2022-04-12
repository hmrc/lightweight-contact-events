/*
 * Copyright 2022 HM Revenue & Customs
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

case class ConfirmedContactDetails(fullName: String,
                                   email: String,
                                   contactNumber: String
                                  )

// TODO - remove ConfirmedContactDetailsLegacy once modernised platform is ready to update to fullName
final case class ConfirmedContactDetailsLegacy(firstName: String,
                                               lastName: String,
                                               email: String,
                                               contactNumber: String)

object ConfirmedContactDetails {
  implicit val format = Json.format[ConfirmedContactDetails]

  def toLegacyContact(ct: ConfirmedContactDetails) =
    ConfirmedContactDetailsLegacy(ct.fullName, lastName = "", ct.email, ct.contactNumber)
}

object ConfirmedContactDetailsLegacy {
  implicit val format = Json.format[ConfirmedContactDetailsLegacy]
}
