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

package uk.gov.hmrc.lightweightcontactevents.connectors

import javax.inject.Inject

import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lightweightcontactevents.utils.AuditServiceConnector
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.ExecutionContext


class AuditingService @Inject()(auditConnector:AuditServiceConnector){


  def sendEvent(auditType: String, json: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier): Unit = {
    val event = eventFor(auditType, json)
    auditConnector.sendExtendedEvent(event)
  }

  private def eventFor(auditType: String, json: JsValue)(implicit hc: HeaderCarrier) = {
    ExtendedDataEvent(
      auditSource = "send-contact-email-api",
      auditType = auditType,
      tags = (hc.headers :+ "transactionName" -> "submit-contact-to-VOA"
        :+ "clientIP" -> hc.trueClientIp.getOrElse("")
        :+ "clientPort" -> hc.trueClientPort.getOrElse("")).toMap-("X-Request-Chain",
        "x-forwarded-for",
        "True-Client-IP",
        "True-Client-Port"),
      detail = json
    )
  }
}
