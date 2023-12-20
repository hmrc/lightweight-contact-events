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

package uk.gov.hmrc.lightweightcontactevents.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, ControllerComponents, DefaultActionBuilder}

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import uk.gov.hmrc.lightweightcontactevents.models.{Contact, QueuedDataTransfer, VOADataTransfer}
import uk.gov.hmrc.lightweightcontactevents.repository.QueuedDataTransferRepository
import uk.gov.hmrc.lightweightcontactevents.utils.Initialize
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

@Singleton
class CreationController @Inject() (
  val queueRepository: QueuedDataTransferRepository,
  val init: Initialize,
  val action: DefaultActionBuilder,
  override val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext
) extends BackendController(controllerComponents) {

  def create(): Action[AnyContent] = action.async { implicit request =>
    createContact(request.body.asJson) match {
      case Right(contact) =>
        val jsonData = VOADataTransfer(contact, init)
        val result   = queueRepository.insert(QueuedDataTransfer(jsonData))

        result.map(_ => Ok)
          .recover {
            case ex: Exception =>
              Logger(getClass).warn("Unable to store email to mongo Queue", ex)
              InternalServerError(s"Unable to store email to mongo Queue: ${ex.getMessage}")
          }
      case Left(error)    =>
        Logger(getClass).warn(error)
        Future.successful(BadRequest(error))
    }
  }

  private[controllers] def createContact(json: Option[JsValue]): Either[String, Contact] =
    json match {
      case Some(value) =>
        val model = Json.fromJson[Contact](value)
        model match {
          case JsSuccess(contact, _) => Right(contact)
          case JsError(_)            => Left("Unable to parse " + value)
        }
      case None        => Left("No Json available")
    }
}
