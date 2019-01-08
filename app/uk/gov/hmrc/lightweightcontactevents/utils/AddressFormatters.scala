/*
 * Copyright 2019 HM Revenue & Customs
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

import uk.gov.hmrc.lightweightcontactevents.models.PropertyAddress

object AddressFormatters {

  def formattedPropertyAddress(address: PropertyAddress, interstitial: String): String = {
    insertInterstitials(Seq(Some(address.addressLine1), address.addressLine2, Some(address.town), address.county, Some(address.postcode)), interstitial)
  }

  private[utils] def insertInterstitials(address: Seq[Option[String]], interstitial: String): String = {
    if (address.isEmpty) "" else trim(address.head, "") + address.tail.foldLeft("") {(acc, elem) => acc + trim(elem, interstitial)}
  }

  def trim(ostr: Option[String], interstitial: String): String = {
    ostr match {
      case Some(s) => interstitial + s.trim
      case None => ""
    }
  }
}
