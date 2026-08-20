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

package uk.gov.hmrc.vo.contact.events.repository

import org.mongodb.scala.SingleObservableFuture
import org.mongodb.scala.bson.collection.immutable.Document
import uk.gov.hmrc.vo.contact.events.models.QueuedDataTransfer
import uk.gov.hmrc.vo.contact.events.util.LightweightITFixture.aQueuedDataTransfer
import uk.gov.hmrc.vo.unit.test.db.MongoDBAppSpec

import java.time.Instant
import java.time.temporal.ChronoUnit

class QueuedDataTransferRepositorySpec extends MongoDBAppSpec[QueuedDataTransfer, QueuedDataTransferRepository]:

  "QueuedDataTransferRepository" should {
    "save item to DB and read it back" in {
      val item = aQueuedDataTransfer()
      mongoRepository.insert(item).futureValue

      val itemFromDb = mongoRepository.findById(item._id).futureValue

      itemFromDb shouldBe Some(item)
    }

    "Update firstError time" in {
      val item = aQueuedDataTransfer()
      mongoRepository.insert(item).futureValue

      val errorTime = Instant.now.truncatedTo(ChronoUnit.MILLIS)

      mongoRepository.updateTime(item._id, errorTime).futureValue

      val itemFromDatabase = mongoRepository.findById(item._id).futureValue

      itemFromDatabase.get.firstError shouldBe Some(errorTime)
    }

    "Get batch of elements" in {
      val items = (1 to 20).map(_ => aQueuedDataTransfer()).toList

      mongoRepository.collection.deleteMany(Document()).toFutureOption().futureValue

      mongoRepository.collection.insertMany(items).toFutureOption().futureValue

      val res = mongoRepository.findBatch().futureValue

      res should have size 10

      items should contain allElementsOf res
    }
  }
