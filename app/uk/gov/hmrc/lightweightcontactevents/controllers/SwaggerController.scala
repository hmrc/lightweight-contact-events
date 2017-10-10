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

import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.mvc._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

// $COVERAGE-OFF$
object SwaggerController extends SwaggerController

trait SwaggerController extends BaseController {

	def specs() = Action.async { implicit request =>
		implicit val cl = getClass.getClassLoader
		val domainPackage = "uk.gov.hmrc.lightweightcontactevents"
		val generator = SwaggerSpecGenerator(domainPackage)
    Future.fromTry(generator.generate("app.routes").map(Ok(_)))
	}
}
// $COVERAGE-ON$
