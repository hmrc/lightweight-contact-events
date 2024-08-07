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

package uk.gov.hmrc.lightweightcontactevents.util

import uk.gov.hmrc.lightweightcontactevents.models.ConfirmedContactDetails.toLegacyContact
import uk.gov.hmrc.lightweightcontactevents.models._

object LightweightITFixture {

  val message                                          = "MSG"
  val subject                                          = "Valuation Office Agency Contact Form"
  val ctEmail                                          = "ct.email@voa.gsi.gov.uk"
  val brEmail                                          = "br.email@voa.gsi.gov.uk"
  val enquiryCategoryMsg                               = "Council Tax"
  val contactReason                                    = "more_details"
  val subEnquiryCategoryMsg                            = "My property is in poor repair or uninhabitable"
  val confirmedContactDetails: ConfirmedContactDetails = ConfirmedContactDetails("full name", "email", "07777777")
  val propertyAddress: PropertyAddress                 = PropertyAddress("line1", Some("line2"), "town", Some("county"), "AA1 1AA")
  val postCode: String                                 = propertyAddress.postcode.replaceAll("\\s+", "").toUpperCase

  val ctContact: Contact = Contact(confirmedContactDetails, propertyAddress, true, contactReason, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val brContact: Contact = Contact(confirmedContactDetails, propertyAddress, false, contactReason, enquiryCategoryMsg, subEnquiryCategoryMsg, message)

  val ctDataTransfer: VOADataTransfer =
    VOADataTransfer(toLegacyContact(confirmedContactDetails), propertyAddress, true, subject, ctEmail, enquiryCategoryMsg, subEnquiryCategoryMsg, message)

  val brDataTransfer: VOADataTransfer =
    VOADataTransfer(toLegacyContact(confirmedContactDetails), propertyAddress, false, subject, brEmail, enquiryCategoryMsg, subEnquiryCategoryMsg, message)
  val wrongContact: Contact           = Contact(confirmedContactDetails, propertyAddress, false, contactReason, "", subEnquiryCategoryMsg, message)

  val housingBenefitEmail           = "NSOhelpdesk@voa.gov.uk"
  val housingBenefitSubject: String = s"CF - other - $postCode"
  val housingBenefitCategory        = "Housing Benefit and Local Housing Allowances"
  val housingBenefitSubCategory     = "I want to find out the valuation details behind my Housing Benefit decision"
  val housingBenefitMessage         = "More about my enquiry..."

  val housingBenefitContact: Contact = Contact(
    confirmedContactDetails,
    propertyAddress,
    isCouncilTaxEnquiry = false,
    "new_enquiry",
    housingBenefitCategory,
    housingBenefitSubCategory,
    housingBenefitMessage
  )

  val housingBenefitDataTransfer: VOADataTransfer = VOADataTransfer(
    toLegacyContact(confirmedContactDetails),
    propertyAddress,
    isCouncilTaxEnquiry = false,
    housingBenefitSubject,
    housingBenefitEmail,
    housingBenefitCategory,
    housingBenefitSubCategory,
    housingBenefitMessage
  )

  def aQueuedDataTransfer(): QueuedDataTransfer =
    QueuedDataTransfer(aVoaDataTransfer())

  def aVoaDataTransfer(): VOADataTransfer =
    VOADataTransfer(
      toLegacyContact(aConfirmedContactDetails()),
      aPropertyAddress(),
      true,
      "Subject",
      "email@email.com",
      "council-tax",
      "subCategory",
      "Free text message"
    )

  def brVoaDataTransfer(): VOADataTransfer =
    VOADataTransfer(
      toLegacyContact(aConfirmedContactDetails()),
      aPropertyAddress(),
      false,
      "Subject",
      "email@email.com",
      "business-rates",
      "subCategory",
      "Free text message"
    )

  def aPropertyAddress(): PropertyAddress =
    PropertyAddress("Some stree", None, "Some town", Some("Some county"), "BN12 4AX")

  def aConfirmedContactDetails(): ConfirmedContactDetails =
    ConfirmedContactDetails(
      "John Doe",
      "email@noreply.voa.gov.uk",
      "0123456789"
    )

}
