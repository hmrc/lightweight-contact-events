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

package uk.gov.hmrc.vo.contact.events

import org.mongodb.scala.SingleObservableFuture
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.vo.unit.test.BaseAppSpec

trait DBIntegrationTest[A] extends BaseAppSpec with DefaultPlayMongoRepositorySupport[A]:

  override protected def afterAll(): Unit =
    mongoComponent.database.drop().toFutureOption().futureValue
    mongoComponent.client.close()
