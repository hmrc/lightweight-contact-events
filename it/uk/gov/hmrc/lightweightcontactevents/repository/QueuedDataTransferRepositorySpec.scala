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

import org.mongodb.scala.bson.collection.immutable.Document

import java.time.Instant
import org.scalatest.OptionValues
import uk.gov.hmrc.lightweightcontactevents.DiAcceptanceTest
import uk.gov.hmrc.lightweightcontactevents.util.LightweightITFixture.aQueuedDataTransfer

class QueuedDataTransferRepositorySpec extends DiAcceptanceTest with OptionValues {

  override def testDbPrefix(): String = "cf-repository-spec"

  def mongoRepository = app.injector.instanceOf[QueuedDataTransferRepository]

  "Repository" should {
    "save item to DB and read it back" in {

      val item = aQueuedDataTransfer()
      await(mongoRepository.insert(item))

      val itemFromDb = await(mongoRepository.findById(item._id))

      itemFromDb.value mustBe item
    }

    "Update firstError time" in {
      val item = aQueuedDataTransfer()
      await(mongoRepository.insert(item))

      val errorTime = Instant.now()

      await(mongoRepository.updateTime(item._id, errorTime))

      val itemFromDatabase = await(mongoRepository.findById(item._id))

      itemFromDatabase.value.firstError.value mustBe errorTime
    }

    "Get batch of elements" in {
      val items = (1 to 20).map(_ => aQueuedDataTransfer()).toList

      await(mongoRepository.collection.deleteMany(Document()).toFutureOption())

      await(mongoRepository.collection.insertMany(items).toFutureOption())

      val res = await(mongoRepository.findBatch())

      res must have size 10

      items must contain allElementsOf (res)
    }

  }

}
