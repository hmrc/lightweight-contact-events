/*
 * Copyright 2020 HM Revenue & Customs
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
import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig


@Singleton
class Initialize @Inject()(conf: Configuration) {
  val subjectTxt = conf.underlying.getString("subject.text")
  val councilTaxEmail = conf.underlying.getString("email.council-tax")
  val businessRatesEmail = conf.underlying.getString("email.non-domestic-rates")
}

class StartupModule extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[Initialize]).asEagerSingleton()
  }
}

class AuditServiceConnector @Inject()(val configuration: Configuration,
                                      environment: Environment,
                                      val auditingConfig: AuditingConfig,
                                      servicesConfig: ServicesConfig)  extends AuditConnector
