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

package uk.gov.hmrc.lightweightcontactevents.utils

import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, Contact, PropertyAddress, QueuedDataTransfer, VOADataTransfer}

object LightweightFixture {

  val message = "MSG"
  val subject = "Valuation Office Agency Contact Form"
  val ctEmail = "ct.email@voa.gsi.gov.uk"
  val brEmail = "br.email@voa.gsi.gov.uk"
  val enquiryCategoryMsg = "Council Tax"
  val contactReason = "more_details"
  val subEnquiryCategoryMsg = "My property is in poor repair or uninhabitable"
  val confirmedContactDetails = ConfirmedContactDetails("full name", "email", "07777777")
  val propertyAddress = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val postCode = propertyAddress.postcode.replaceAll("\\s+","").toUpperCase
  val ctContact = Contact(confirmedContactDetails, propertyAddress, contactReason, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val brContact = Contact(confirmedContactDetails, propertyAddress, contactReason, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val ctDataTransfer = VOADataTransfer(confirmedContactDetails, propertyAddress, subject, ctEmail, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val brDataTransfer = VOADataTransfer(confirmedContactDetails, propertyAddress, subject, brEmail, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val wrongContact = Contact(confirmedContactDetails, propertyAddress, contactReason, "", subEnquiryCategoryMsg, message)

  def aQueuedDataTransfer(): QueuedDataTransfer = {
    QueuedDataTransfer(aVoaDataTransfer())
  }

  def aVoaDataTransfer(): VOADataTransfer = {
    VOADataTransfer(aConfirmedContactDetails(), aPropertyAddress(),
      "Subject", "email@email.com", "council-tax", "subCategory", "Free text message")
  }

  def brVoaDataTransfer(): VOADataTransfer = {
    VOADataTransfer(aConfirmedContactDetails(), aPropertyAddress(),
      "Subject", "email@email.com", "business-rates", "subCategory", "Free text message")
  }

  def aPropertyAddress(): PropertyAddress = {
    PropertyAddress("Some stree", None, "Some town", Some("Some county"), "BN12 4AX")
  }

  def aConfirmedContactDetails()  = {
    ConfirmedContactDetails(
      "John Doe",
      "email@noreply.voa.gov.uk",
      "0123456789"
    )
  }

}
