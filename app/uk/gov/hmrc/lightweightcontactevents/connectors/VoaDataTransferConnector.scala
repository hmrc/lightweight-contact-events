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

package uk.gov.hmrc.lightweightcontactevents.connectors

import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.lightweightcontactevents.models.VOADataTransfer
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class VoaDataTransferConnector @Inject()(val http: HttpClient,
                                         val configuration: Configuration,
                                         environment: Environment,
                                          auditService:AuditingService,
                                         servicesConfig: ServicesConfig
                                        ) {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val jsonContentTypeHeader = ("Content-Type", "application/json")

  val serviceUrl: String = servicesConfig.baseUrl("voa-data-transfer")
  val RETURN_200: Int = 200

  def transfer(dataTransfer: VOADataTransfer)(implicit hc: HeaderCarrier): Future[Try[Int]] = sendJson(Json.toJson(dataTransfer))

  private[connectors] def sendJson(json: JsValue)(implicit hc: HeaderCarrier): Future[Try[Int]] =
    http.POST[JsValue, HttpResponse](s"$serviceUrl/contact-process-api/contact/sendemail", json, Seq(jsonContentTypeHeader)).map { response =>
      auditService.sendEvent("sendcontactemailtoVOA", json)
    response.status match {
      case RETURN_200 =>
        Success(RETURN_200)
      case status =>
        Logger(getClass).warn("Data transfer service fails with status " + status)
        Failure(new RuntimeException("Data transfer service fails with status " + status))
    }
  } recover {
      case ex =>
        Logger(getClass).warn("Data transfer service fails with exception " + ex.getMessage)
        Failure(new RuntimeException("Data transfer service fails with exception " + ex.getMessage))
    }

}
