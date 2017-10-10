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

package uk.gov.hmrc.lightweightcontactevents.controllers

import javax.inject.Inject

import play.api.mvc.{Action, AnyContent, Controller, Results}

import scala.concurrent.Future
import play.api.libs.json._
import uk.gov.hmrc.lightweightcontactevents.models.ContactModel


class CreationController @Inject()() extends Controller {

  def createContact(json: Option[JsValue]): Either[String, ContactModel] = {
    json match {
      case Some(value) => {
        val model = Json.fromJson[ContactModel](value)
        model match {
          case JsSuccess(m, _) => if (m.councilTaxAddress.isDefined && m.businessRatesAddress.isDefined) {
            Left("Json contains both council tax address and business rates address")
          } else {
            Right(m)
          }
          case JsError(_) => Left("Unable to parse " + value)
        }
      }
      case None => Left("No Json available")
    }
  }

  def create(): Action[AnyContent] = Action.async {implicit request =>
    createContact(request.body.asJson) match {
      case Right(m) => Future.successful(Ok("X6T67FG"))
      case Left(error) => {
        Future.successful(BadRequest(error))
      }
    }
  }
}
