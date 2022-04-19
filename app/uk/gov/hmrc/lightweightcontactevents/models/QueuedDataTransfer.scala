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

package uk.gov.hmrc.lightweightcontactevents.models

import org.bson.types.ObjectId
import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import scala.util.Try

case class QueuedDataTransfer(voaDataTransfer: VOADataTransfer, firstError: Option[Instant] = None, _id: ObjectId = ObjectId.get())


object QueuedDataTransfer {

  import uk.gov.hmrc.mongo.play.json.formats.MongoFormats.Implicits._

  implicit val instantWrites: Writes[Instant] = {
    case instant: Instant => JsString(instant.atZone(ZoneOffset.UTC).toString)
    case _ => JsNull
  }

  implicit val instantReads: Reads[Instant] = Reads[Instant] {
    case JsString(str) =>
      Try(JsSuccess(ZonedDateTime.parse(str).toInstant))
        .getOrElse(JsError("error.invalid.dateformat"))
    case _ => JsError("error.expected.string")
  }

  implicit val instantFormat: Format[Instant] = Format(instantReads, instantWrites)

  implicit val format: Format[QueuedDataTransfer] = Json.format[QueuedDataTransfer]

}
