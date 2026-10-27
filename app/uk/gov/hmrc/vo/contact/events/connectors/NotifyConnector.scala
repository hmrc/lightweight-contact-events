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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vo.contact.events.models.VODataTransfer
import uk.gov.hmrc.vo.contact.events.models.event.SendContactEmailToVOA
import uk.gov.hmrc.vo.service.connectors.{AuditService, GovNotifyConnector}
import uk.gov.service.notify.SendEmailResponse

import javax.inject.{Inject, Singleton}
import scala.util.Try

/**
  * GOV.UK Notify REST API client.
  *
  * @author Yuriy Tumakha
  */
@Singleton
class NotifyConnector @Inject() (config: Configuration, audit: AuditService) extends GovNotifyConnector(config):

  private val contactFormTemplateId = config.get[String]("notify.contactFormTemplateId")

  def sendEmailToVO(data: VODataTransfer)(using hc: HeaderCarrier): Try[SendEmailResponse] =
    val personalisationMap: Map[String, String] = Map(
      "subject"               -> data.subject,
      "propertyAddress"       -> data.propertyAddress.singleLine,
      "firstName"             -> data.contact.firstName,
      "lastName"              -> data.contact.lastName,
      "email"                 -> data.contact.email,
      "contactNumber"         -> data.contact.contactNumber,
      "enquiryCategoryMsg"    -> data.enquiryCategoryMsg,
      "subEnquiryCategoryMsg" -> data.subEnquiryCategoryMsg,
      "message"               -> data.message
    )

    audit.send(SendContactEmailToVOA(data))
    sendEmail(contactFormTemplateId, data.recipientEmailAddress, personalisationMap, data.subject)
