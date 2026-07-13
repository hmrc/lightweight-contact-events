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

package uk.gov.hmrc.vo.contact.events.models

import org.bson.types.ObjectId
import play.api.libs.json.*

import java.time.Instant
import java.time.temporal.ChronoUnit

case class QueuedDataTransfer(
  voDataTransfer: VODataTransfer,
  firstError: Option[Instant] = None,
  _id: ObjectId = ObjectId.get(),
  createdAt: Instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
)

object QueuedDataTransfer:

  import uk.gov.hmrc.mongo.play.json.formats.MongoFormats.Implicits.*

  import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits.*

  implicit val format: Format[QueuedDataTransfer] = Json.format[QueuedDataTransfer]
