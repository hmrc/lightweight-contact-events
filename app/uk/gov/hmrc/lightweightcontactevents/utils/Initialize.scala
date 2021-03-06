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

import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import com.google.inject.AbstractModule
import play.api.inject.ApplicationLifecycle
import play.api.Configuration
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

@Singleton
class Initialize @Inject()(conf: Configuration) {
  val subjectText = conf.underlying.getString("subject.text")
  val subjectAddInfo = conf.underlying.getString("subject.additional-info.text")
  val subjectChase = conf.underlying.getString("subject.chase.text")
  val subjectOtherAddInfo = conf.underlying.getString("subject.other-additional-info.text")
  val subjectOtherChase = conf.underlying.getString("subject.other-chase.text")
  val councilTaxEmail = conf.underlying.getString("email.council-tax")
  val businessRatesEmail = conf.underlying.getString("email.business-rates")
  val housingAllowanceEmail = conf.underlying.getString("email.housing-allowance")
  val otherEmail = conf.underlying.getString("email.other")
}

class StartupModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Initialize]).asEagerSingleton()
  }
}

class AuditServiceConnector @Inject()(val configuration: Configuration,
                                      val materializer: Materializer,
                                      val auditingConfig: AuditingConfig,
                                      val lifecycle: ApplicationLifecycle)  extends AuditConnector
