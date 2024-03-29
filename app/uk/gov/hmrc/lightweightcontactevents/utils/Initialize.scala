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

package uk.gov.hmrc.lightweightcontactevents.utils

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class Initialize @Inject() (conf: Configuration) {
  val subjectText: String           = conf.underlying.getString("subject.text")
  val subjectAddInfo: String        = conf.underlying.getString("subject.additional-info.text")
  val subjectChase: String          = conf.underlying.getString("subject.chase.text")
  val subjectOtherAddInfo: String   = conf.underlying.getString("subject.other-additional-info.text")
  val subjectOtherChase: String     = conf.underlying.getString("subject.other-chase.text")
  val councilTaxEmail: String       = conf.underlying.getString("email.council-tax")
  val businessRatesEmail: String    = conf.underlying.getString("email.business-rates")
  val housingAllowanceEmail: String = conf.underlying.getString("email.housing-allowance")
  val otherEmail: String            = conf.underlying.getString("email.other")
}
