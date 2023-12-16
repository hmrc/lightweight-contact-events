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

package uk.gov.hmrc.lightweightcontactevents.infrastructure

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.TestProbe
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.OptionValues
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lightweightcontactevents.DiAcceptanceTest
import uk.gov.hmrc.lightweightcontactevents.connectors.{AuditingService, VoaDataTransferConnector}
import uk.gov.hmrc.lightweightcontactevents.models.VOADataTransfer
import uk.gov.hmrc.lightweightcontactevents.repository.QueuedDataTransferRepository
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.lightweightcontactevents.util.LightweightITFixture.aQueuedDataTransfer
import uk.gov.hmrc.mongo.lock.MongoLockRepository

import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Success, Try}

class ExportTransferSpec extends DiAcceptanceTest with OptionValues {
  override def testDbPrefix(): String = "ExportTransferSpec"

  override def fakeApplicationBuilder(): GuiceApplicationBuilder = super.fakeApplicationBuilder()
    .configure(Map(
      "voaExport.enable" -> false
    )).overrides(new AbstractModule with ScalaModule {
    override def configure(): Unit = {
      bind[Clock].toInstance(Clock.systemUTC())
      bind[VoaDataTransferConnector].to[ExportTestDataTransferConnector]
    }
  })

  def exportConnector: ExportTestDataTransferConnector = app.injector.instanceOf[ExportTestDataTransferConnector]

  "Scheduler" should {
    "Schedule event and export data to VOA" in {

      exportConnector.transfer = List.empty[VOADataTransfer]

      implicit val actorSystem: ActorSystem = app.actorSystem
      implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

      val probe = TestProbe("test-probe")
      actorSystem.eventStream.subscribe(probe.ref, classOf[ExportEvent]) //subscribe for event

      val transfer = aQueuedDataTransfer()
      await(repository.insert(transfer))  //item is in database, can trigger scheduler

      val scheduler = createScheduler()
      scheduler.start()

      val exportEvent = probe.expectMsgType[ExportEvent](3 seconds)

      val queueSize = await(repository.count)

      exportEvent mustBe ExportSuccess
      queueSize.value mustBe 0

      exportConnector.transfer.head mustBe transfer.voaDataTransfer
    }

    "Keep items in DB if export fail" in {
      exportConnector.transfer = List.empty[VOADataTransfer]
      exportConnector.responseCode = NOT_FOUND

      implicit val actorSystem: ActorSystem = app.actorSystem
      implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

      val probe = TestProbe("test-probe")
      actorSystem.eventStream.subscribe(probe.ref, classOf[ExportEvent]) //subscribe for event

      val transfer = aQueuedDataTransfer()
      await(repository.insert(transfer))  //item is in database, can trigger scheduler

      val scheduler = createScheduler()
      scheduler.start()

      val exportEvent = probe.expectMsgType[ExportEvent](3 seconds)

      val queueSize = await(repository.count)

      exportEvent mustBe ExportSuccess
      queueSize.value mustBe 1
    }

  }


  def repository: QueuedDataTransferRepository = app.injector.instanceOf[QueuedDataTransferRepository]


  def createScheduler(): VoaDataTransferScheduler = {
    val actorSystem = app.injector.instanceOf[ActorSystem]
    val mongoLockRepository: MongoLockRepository = app.injector.instanceOf[MongoLockRepository]

    implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

    new VoaDataTransferScheduler(
      actorSystem.scheduler,
      actorSystem.eventStream,
      new ScheduleEvery1Second(),
      app.injector.instanceOf[VoaDataTransferExporter],
      mongoLockRepository
    )
  }

}

class ScheduleEvery1Second extends DefaultRegularSchedule {
  override def timeUntilNextRun(): FiniteDuration = FiniteDuration.apply(1, TimeUnit.SECONDS)
}

@Singleton
class ExportTestDataTransferConnector @Inject() (http: HttpClient,
                                                  configuration: Configuration,
                                                  auditService:AuditingService,
                                                 servicesConfig: ServicesConfig)(implicit ec: ExecutionContext)
  extends VoaDataTransferConnector(http, configuration, auditService, servicesConfig) {

  var transfer: List[VOADataTransfer] = List[VOADataTransfer]()
  var responseCode: Int = OK

  override def transfer(dataTransfer: VOADataTransfer)(implicit hc: HeaderCarrier): Future[Try[Int]] = {
    transfer = dataTransfer :: transfer
    Future.successful(Success(responseCode))
  }

}
