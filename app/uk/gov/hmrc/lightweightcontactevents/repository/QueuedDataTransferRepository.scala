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

import org.mongodb.scala.ReadPreference
import uk.gov.hmrc.lightweightcontactevents.models.QueuedDataTransfer
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class QueuedDataTransferRepository @Inject()(
                                              mongo: MongoComponent
                                            )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[QueuedDataTransfer](
    collectionName = "dataTransferQueue",
    mongoComponent = mongo,
    domainFormat = QueuedDataTransfer.format,
    indexes = Seq.empty) {

  val defaultBatchSize = 10

  def updateTime(id: String, time: Instant): Future[Unit] = {
    //    collection.findOneAndUpdate(equal("_id", Codecs.toBson(id))
    //
    //    val selector = _id(id)
    //
    //    val update = Json.obj(
    //      "$set" -> Json.obj(
    //        "fistError" -> time
    //      )
    //    )
    //
    //    collection.findOneAndUpdate(filter = selector, update = update, options = FindOneAndUpdateOptions()).toFutureOption.map(_ => ())

    Future.unit
  }

  def findBatch(batchSize: Int = defaultBatchSize,
                readPreference: ReadPreference = ReadPreference.primaryPreferred()
               ): Future[List[QueuedDataTransfer]] = {

    //    collection.find(Json.obj(), Option.empty[JsObject]).options(QueryOpts().batchSize(batchSize))
    //        .cursor[QueuedDataTransfer](readPreference)
    //        .collect[List](batchSize, FailOnError[List[QueuedDataTransfer]]())

    Future.successful(List.empty)
  }

  def insert(transfer: QueuedDataTransfer): Future[Unit] = ???

  def removeById(id: String): Future[Unit] = ???

}
