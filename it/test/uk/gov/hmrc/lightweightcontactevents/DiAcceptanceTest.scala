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

package uk.gov.hmrc.lightweightcontactevents

import org.mongodb.scala.SingleObservableFuture
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.PlayMongoModule

trait DiAcceptanceTest extends AnyWordSpecLike with BeforeAndAfterAll with Matchers with FutureAwaits with DefaultAwaitTimeout with GuiceOneAppPerSuite {

  def testDbPrefix(): String

  def fakeApplicationBuilder(): GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .configure(customConfigs)
    .bindings(new PlayMongoModule)

  def testDbName: String = s"${testDbPrefix()}${java.util.UUID.randomUUID.toString.replaceAll("-", "")}"

  lazy val testDbUri: String = s"mongodb://localhost:27017/$testDbName"

  def customConfigs: Map[String, Any] = Map(
    "mongodb.uri" -> testDbUri
  )

  def mongoComponent: MongoComponent = app.injector.instanceOf[MongoComponent]

  final override def fakeApplication(): Application = fakeApplicationBuilder().build()

  override protected def afterAll(): Unit = {
    await(mongoComponent.database.drop().toFutureOption())
    mongoComponent.client.close()
  }

}
