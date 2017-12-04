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

package uk.gov.hmrc.lightweightcontactevents.services

import com.eclipsesource.schema.{SchemaType, SchemaValidator}
import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json._

@Singleton
class JSONSchemaValidationService @Inject()(conf: Configuration) {

  private val validationSchema: SchemaType = {
    val schemaStr = conf.underlying.getString("schema")
    Json.fromJson[SchemaType](Json.parse(schemaStr)).getOrElse(sys.error("Could not parse schema string"))
  }

  private lazy val jsonValidator: SchemaValidator = new SchemaValidator()

  def validateAgainstSchema(contact: Option[JsValue]): Either[String, JsValue] =
    contact match {
      case Some(value) =>
        jsonValidator.validate(validationSchema, value) match {
          case e: JsError ⇒ Left(s"Content was not valid against schema: ${e.toString}")
          case JsSuccess(c, _) ⇒ Right(c)
        }
      case None => Left("No Json available")
    }
}
