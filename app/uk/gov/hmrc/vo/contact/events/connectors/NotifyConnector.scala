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

import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vo.contact.events.models.VODataTransfer
import uk.gov.hmrc.vo.service.connectors.GovNotifyConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * GOV.UK Notify REST API client.
  *
  * @author Yuriy Tumakha
  */
@Singleton
class NotifyConnector @Inject() (config: Configuration, auditService: AuditingService)(using ec: ExecutionContext) extends GovNotifyConnector(config):

  private val contactFormTemplateId = config.get[String]("notify.contactFormTemplateId")

  def sendEmailToVO(data: VODataTransfer)(using hc: HeaderCarrier): Try[Unit] =
    val json = Json.toJson(data)

    val personalisationMap: Map[String, String] = null

    sendEmail(contactFormTemplateId, data.recipientEmailAddress, personalisationMap, data.subject)
      .map { _ =>
        auditService.sendEvent("sendcontactemailtoVOA", json)
      }
