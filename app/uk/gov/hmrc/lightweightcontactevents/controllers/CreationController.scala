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
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Controller}
import scala.concurrent.Future
import play.api.libs.json._
import uk.gov.hmrc.lightweightcontactevents.models.Contact


class CreationController @Inject()() extends Controller {

  def createContact(json: Option[JsValue]): Either[String, Contact] = {
    json match {
      case Some(value) => {
        val model = Json.fromJson[Contact](value)
        model match {
          case JsSuccess(contact, _) => contact match {
            case Contact(_, None, None, _, _, _) => Left("Json contains neither council tax address and business rates address")
            case Contact(_, councilTaxAddress, None, _, _, _) => Right(contact)
            case Contact(_, None, businessRatesAddress, _, _, _) => Right(contact)
            case Contact(_, councilTaxAddress, businessRatesAddress, _, _, _) => Left("Json contains both council tax address and business rates address")
          }
          case JsError(_) => Left("Unable to parse " + value)
        }
      }
      case None => Left("No Json available")
    }
  }

  def create(): Action[AnyContent] = Action.async {implicit request =>
    createContact(request.body.asJson) match {
      case Right(contact) => Future.successful(Ok)
      case Left(error) => {
        Logger.warn(error)
        Future.successful(BadRequest(error))
      }
    }
  }
}
