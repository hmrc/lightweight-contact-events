/*
 * Copyright 2017 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lightweightcontactevents.models.Email
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.BaseUrl

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class EmailConnector @Inject()(val http: HttpClient, override val configuration: Configuration) extends BaseUrl {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  val domain = "/hmrc/"
  val jsonContentTypeHeader = ("Content-Type", "application/json")
  lazy val serviceUrl = baseUrl("email")

  def sendJson(json: JsValue): Future[Try[Int]] =
    http.POST(s"$serviceUrl${domain}email", json, Seq(jsonContentTypeHeader)).map { response =>
      response.status match {
        case 202 => Success(200)
        case status =>
          Logger.warn("Email service fails with status " + status)
          Failure(new RuntimeException("Email service fails with status " + status))
      }
    }

  def sendEmail(email: Email): Future[Try[Int]] = sendJson(Json.toJson(email))
}