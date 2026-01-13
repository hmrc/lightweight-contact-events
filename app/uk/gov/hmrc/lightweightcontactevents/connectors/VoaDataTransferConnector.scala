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

package uk.gov.hmrc.lightweightcontactevents.connectors

import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.lightweightcontactevents.models.VOADataTransfer
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class VoaDataTransferConnector @Inject() (
  httpClientV2: HttpClientV2,
  auditService: AuditingService,
  servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext
) extends Logging:

  private val jsonContentTypeHeader: (String, String) = "Content-Type" -> "application/json"

  private val dataTransferBaseUrl: String = servicesConfig.baseUrl("voa-data-transfer")

  def transfer(dataTransfer: VOADataTransfer)(implicit hc: HeaderCarrier): Future[Try[Int]] = sendJson(Json.toJson(dataTransfer))

  private[connectors] def sendJson(json: JsValue)(implicit hc: HeaderCarrier): Future[Try[Int]] =
    val url = s"$dataTransferBaseUrl/contact-process-api/contact/sendemail"
    httpClientV2.post(url"$url")
      .withBody(json)
      .setHeader(jsonContentTypeHeader)
      .execute[HttpResponse]
      .map { r =>
        auditService.sendEvent("sendcontactemailtoVOA", json)
        r.status match {
          case status if is2xx(status) => Success(OK)
          case status                  =>
            logger.warn("Data transfer service fails with status " + status)
            Failure(new RuntimeException("Data transfer service fails with status " + status))
        }
      }
      .recover {
        case ex =>
          logger.warn("Data transfer service fails with exception " + ex.getMessage)
          Failure(new RuntimeException("Data transfer service fails with exception " + ex.getMessage))
      }
