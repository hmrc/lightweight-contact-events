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

package uk.gov.hmrc.lightweightcontactevents.repository

import java.time.Instant

import javax.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.lightweightcontactevents.models.QueuedDataTransfer
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class QueuedDataTransferRepository @Inject() (mongo: ReactiveMongoComponent) extends ReactiveRepository[QueuedDataTransfer,BSONObjectID] (
  collectionName =  "dataTransferQueue",
  mongo = mongo.mongoConnector.db,
  domainFormat = QueuedDataTransfer.format,
  idFormat = ReactiveMongoFormats.objectIdFormats) {

  val defaultBatchSize = 10

  def updateTime(id: BSONObjectID, time: Instant)(implicit ec: ExecutionContext): Future[Unit] = {
    val selector = _id(id)

    val update = Json.obj(
      "$set" -> Json.obj(
        "fistError" -> time
      )
    )

    findAndUpdate(selector, update).map(_ => ())
  }

  def findBatch(batchSize: Int = defaultBatchSize,
                readPreference: ReadPreference = ReadPreference.primaryPreferred
               )(implicit ec: ExecutionContext):Future[List[QueuedDataTransfer]] = {

    collection.find(Json.obj(), Option.empty[JsObject]).options(QueryOpts().batchSize(batchSize))
        .cursor[QueuedDataTransfer](readPreference)
        .collect[List](batchSize, FailOnError[List[QueuedDataTransfer]]())

  }


}

