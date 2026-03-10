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

package uk.gov.hmrc.vo.contact.events.infrastructure

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.testkit.TestProbe
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.OptionValues
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.vo.contact.events.util.LightweightITFixture.aQueuedDataTransfer
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.vo.contact.events.DiAcceptanceTest
import uk.gov.hmrc.vo.contact.events.connectors.{AuditingService, VODataTransferConnector}
import uk.gov.hmrc.vo.contact.events.models.VODataTransfer
import uk.gov.hmrc.vo.contact.events.repository.QueuedDataTransferRepository

import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Success, Try}

class ExportTransferSpec extends DiAcceptanceTest with OptionValues:
  override def testDbPrefix(): String = "ExportTransferSpec"

  override def fakeApplicationBuilder(): GuiceApplicationBuilder = super.fakeApplicationBuilder()
    .configure(Map(
      "voaExport.enable" -> false
    )).overrides(
      new AbstractModule with ScalaModule:
        override def configure(): Unit =
          bind[Clock].toInstance(Clock.systemUTC)
          bind[VODataTransferConnector].to[ExportTestDataTransferConnector]
    )

  def exportConnector: ExportTestDataTransferConnector = app.injector.instanceOf[ExportTestDataTransferConnector]

  "Scheduler" should {
    "Schedule event and export data to VO" in {

      exportConnector.transfer = List.empty[VODataTransfer]

      implicit val actorSystem: ActorSystem = app.actorSystem
      implicit val ec: ExecutionContext     = app.injector.instanceOf[ExecutionContext]

      val probe = TestProbe("test-probe")
      actorSystem.eventStream.subscribe(probe.ref, classOf[ExportEvent]) // subscribe for event

      val transfer = aQueuedDataTransfer()
      await(repository.insert(transfer)) // item is in database, can trigger scheduler

      val scheduler = createScheduler()
      scheduler.start()

      val exportEvent = probe.expectMsgType[ExportEvent](3 seconds)

      val queueSize = await(repository.count)

      exportEvent mustBe ExportSuccess
      queueSize.value mustBe 0

      exportConnector.transfer.head mustBe transfer.voDataTransfer
    }

    "Keep items in DB if export fail" in {
      exportConnector.transfer = List.empty[VODataTransfer]
      exportConnector.responseCode = NOT_FOUND

      implicit val actorSystem: ActorSystem = app.actorSystem
      implicit val ec: ExecutionContext     = app.injector.instanceOf[ExecutionContext]

      val probe = TestProbe("test-probe")
      actorSystem.eventStream.subscribe(probe.ref, classOf[ExportEvent]) // subscribe for event

      val transfer = aQueuedDataTransfer()
      await(repository.insert(transfer)) // item is in database, can trigger scheduler

      val scheduler = createScheduler()
      scheduler.start()

      val exportEvent = probe.expectMsgType[ExportEvent](3 seconds)

      val queueSize = await(repository.count)

      exportEvent mustBe ExportSuccess
      queueSize.value mustBe 1
    }

  }

  def repository: QueuedDataTransferRepository = app.injector.instanceOf[QueuedDataTransferRepository]

  def createScheduler(): VODataTransferScheduler =
    val actorSystem                              = app.injector.instanceOf[ActorSystem]
    val mongoLockRepository: MongoLockRepository = app.injector.instanceOf[MongoLockRepository]

    implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

    VODataTransferScheduler(
      actorSystem.scheduler,
      actorSystem.eventStream,
      ScheduleEvery1Second(),
      app.injector.instanceOf[VODataTransferExporter],
      mongoLockRepository
    )

class ScheduleEvery1Second extends DefaultRegularSchedule:
  override def timeUntilNextRun(): FiniteDuration = FiniteDuration.apply(1, TimeUnit.SECONDS)

@Singleton
class ExportTestDataTransferConnector @Inject() (
  httpClientV2: HttpClientV2,
  auditService: AuditingService,
  servicesConfig: ServicesConfig
)(using ec: ExecutionContext
) extends VODataTransferConnector(httpClientV2, auditService, servicesConfig):

  var transfer: List[VODataTransfer] = List[VODataTransfer]()
  var responseCode: Int              = OK

  override def transfer(dataTransfer: VODataTransfer)(using hc: HeaderCarrier): Future[Try[Int]] =
    transfer = dataTransfer :: transfer
    Future.successful(Success(responseCode))
