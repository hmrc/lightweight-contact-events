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

import org.bson.codecs.ObjectIdCodec
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.model.{Filters, FindOneAndReplaceOptions, FindOneAndUpdateOptions, ReturnDocument}
import org.mongodb.scala.{ReadPreference, SingleObservable}
import play.api.Logging
import uk.gov.hmrc.lightweightcontactevents.models.QueuedDataTransfer
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class QueuedDataTransferRepository @Inject() (
  mongo: MongoComponent
)(implicit ec: ExecutionContext
) extends PlayMongoRepository[QueuedDataTransfer](
    collectionName = "dataTransferQueue",
    mongoComponent = mongo,
    domainFormat = QueuedDataTransfer.format,
    indexes = Seq.empty,
    extraCodecs = Seq(
      new ObjectIdCodec,
      Codecs.playFormatCodec(QueuedDataTransfer.instantFormat)
    )
  )
  with Logging {

  val _id              = "_id"
  val defaultBatchSize = 10

  implicit class singleObservableOps[T](singleObservable: SingleObservable[T]) {

    def toFutureUnit: Future[Unit] = singleObservable
      .toFutureOption()
      .map(_ => ())
      .recover {
        case ex: Throwable =>
          logger.error("Mongo error", ex)
      }
  }

  def updateTime(id: ObjectId, time: Instant): Future[Unit] =
    collection
      .findOneAndUpdate(byId(id), set("firstError", time), FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER))
      .toFutureOption()
      .flatMap {
        case Some(_) => Future.unit
        case _       => Future.failed(new IllegalStateException(s"dataTransfer not found for id = $id"))
      }

  def findBatch(batchSize: Int = defaultBatchSize, readPreference: ReadPreference = ReadPreference.primaryPreferred()): Future[Seq[QueuedDataTransfer]] =
    collection.withReadPreference(readPreference)
      .find()
      .limit(batchSize)
      .toFuture()

  def insert(transfer: QueuedDataTransfer): Future[Unit] =
    collection.findOneAndReplace(byId(transfer._id), transfer, FindOneAndReplaceOptions().upsert(true))
      .toFutureUnit

  def findById(id: ObjectId, readPreference: ReadPreference = ReadPreference.primaryPreferred()): Future[Option[QueuedDataTransfer]] =
    collection.withReadPreference(readPreference)
      .find(byId(id)).first()
      .toFutureOption()

  def removeById(id: ObjectId): Future[Unit] =
    collection.deleteOne(byId(id))
      .toFutureUnit

  def count: Future[Option[Long]] =
    collection.countDocuments()
      .toFutureOption()

  private def byId(id: ObjectId): Bson =
    Filters.equal(_id, id)

}
