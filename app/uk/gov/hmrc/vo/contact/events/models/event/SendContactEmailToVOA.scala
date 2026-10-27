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

package uk.gov.hmrc.vo.contact.events.models.event

import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.vo.contact.events.models.{ConfirmedContactDetailsLegacy, PropertyAddress, VODataTransfer}
import uk.gov.hmrc.vo.service.model.event.AuditEvent

/**
  * @author Yuriy Tumakha
  */
case class SendContactEmailToVOA(
  contact: ConfirmedContactDetailsLegacy,
  propertyAddress: PropertyAddress,
  isCouncilTaxEnquiry: Boolean,
  subject: String,
  recipientEmailAddress: String,
  enquiryCategoryMsg: String,
  subEnquiryCategoryMsg: String,
  message: String
) extends AuditEvent

object SendContactEmailToVOA:
  implicit val writes: OWrites[SendContactEmailToVOA] = Json.writes

  def apply(data: VODataTransfer): SendContactEmailToVOA =
    SendContactEmailToVOA(
      contact = data.contact,
      propertyAddress = data.propertyAddress,
      isCouncilTaxEnquiry = data.isCouncilTaxEnquiry,
      subject = data.subject,
      recipientEmailAddress = data.recipientEmailAddress,
      enquiryCategoryMsg = data.enquiryCategoryMsg,
      subEnquiryCategoryMsg = data.subEnquiryCategoryMsg,
      message = data.message
    )
