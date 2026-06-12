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

package uk.gov.hmrc.vo.contact.events.connectors

import uk.gov.hmrc.vo.contact.events.models.ConfirmedContactDetails.toLegacyContact
import uk.gov.hmrc.vo.contact.events.models.{ConfirmedContactDetails, PropertyAddress, VODataTransfer}
import uk.gov.hmrc.vo.unit.test.BaseAppSpec

import scala.util.Success

/**
  * @author Yuriy Tumakha
  */
class NotifyConnectorSpec extends BaseAppSpec:

  private val notifyConnector = inject[NotifyConnector]

  private val propertyAddress = PropertyAddress("Some street", None, "Some town", Some("Some county"), "BN12 4AX")
  private val contactDetails  = ConfirmedContactDetails("John Doe", "noreply@vo.hmrc.gov.uk", "0123456789")

  private val dataTransfer =
    VODataTransfer(
      toLegacyContact(contactDetails),
      propertyAddress,
      true,
      "Subject",
      "email@email.com",
      "council-tax",
      "subCategory",
      "Free text message"
    )

  "NotifyConnector" should {
    "sendEmailToVO" in {
      notifyConnector.sendEmailToVO(dataTransfer) shouldBe Success(())
    }
  }
