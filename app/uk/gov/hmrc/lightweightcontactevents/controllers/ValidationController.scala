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

import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Logger}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.lightweightcontactevents.connectors.EmailConnector
import uk.gov.hmrc.lightweightcontactevents.models.Email
import uk.gov.hmrc.lightweightcontactevents.utils.Initialize
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import uk.gov.hmrc.lightweightcontactevents.services.JSONSchemaValidationService

@Singleton
class ValidationController @Inject()(conf: Configuration) extends BaseController {


  def validate(): Action[AnyContent] = Action { implicit request =>
    new JSONSchemaValidationService(conf).validateAgainstSchema(request.body.asJson) match {
      case Right(contact) => Ok
      case Left(error) => {
        Logger.warn(error)
        BadRequest(error)
      }
    }
  }
}