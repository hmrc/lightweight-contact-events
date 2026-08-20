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
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.vo.contact.events.connectors.{AuditingService, NotifyConnector}
import uk.gov.hmrc.vo.contact.events.models.{QueuedDataTransfer, VODataTransfer}
import uk.gov.hmrc.vo.contact.events.repository.QueuedDataTransferRepository
import uk.gov.hmrc.vo.contact.events.util.LightweightITFixture.aQueuedDataTransfer
import uk.gov.hmrc.vo.unit.test.db.MongoDBAppSpec

import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class ExportToNotifySpec extends MongoDBAppSpec[QueuedDataTransfer, QueuedDataTransferRepository]:

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        Map(
          "voaExport.enable" -> false
        )
      )
      .overrides(
        bind[MongoComponent].toInstance(mongoComponent),
        bind[Clock].toInstance(Clock.systemUTC),
        bind[NotifyConnector].to[TestNotifyConnector]
      )
      .build()

  val testNotifyConnector: TestNotifyConnector = inject[TestNotifyConnector]

  "Scheduler" should {
    "Schedule event and export data to VO" in {
      testNotifyConnector.transfer = List.empty[VODataTransfer]
      testNotifyConnector.response = Success(())

      implicit val actorSystem: ActorSystem = app.actorSystem

      val probe = TestProbe("test-probe")
      actorSystem.eventStream.subscribe(probe.ref, classOf[ExportEvent]) // subscribe for event

      val transfer = aQueuedDataTransfer()
      mongoRepository.insert(transfer).futureValue // item is in database, can trigger scheduler

      val scheduler = createScheduler()
      scheduler.start()

      val exportEvent = probe.expectMsgType[ExportEvent](3 seconds)

      val queueSize = mongoRepository.count.futureValue

      exportEvent shouldBe ExportSuccess
      queueSize   shouldBe Some(0)

      testNotifyConnector.transfer.head shouldBe transfer.voDataTransfer
    }

    "Keep items in DB if export fail" in {
      testNotifyConnector.transfer = List.empty[VODataTransfer]
      testNotifyConnector.response = Failure(RuntimeException("Send email failure"))

      implicit val actorSystem: ActorSystem = app.actorSystem

      val probe = TestProbe("test-probe")
      actorSystem.eventStream.subscribe(probe.ref, classOf[ExportEvent]) // subscribe for event

      val transfer = aQueuedDataTransfer()
      mongoRepository.insert(transfer).futureValue // item is in database, can trigger scheduler

      val scheduler = createScheduler()
      scheduler.start()

      val exportEvent = probe.expectMsgType[ExportEvent](3 seconds)

      val queueSize = mongoRepository.count.futureValue

      exportEvent shouldBe ExportSuccess
      queueSize   shouldBe Some(1)
    }
  }

  def createScheduler(): VODataTransferScheduler =
    val actorSystem                              = inject[ActorSystem]
    val mongoLockRepository: MongoLockRepository = inject[MongoLockRepository]

    VODataTransferScheduler(
      actorSystem.scheduler,
      actorSystem.eventStream,
      ScheduleEvery1Second(),
      inject[VODataTransferExporter],
      mongoLockRepository
    )

class ScheduleEvery1Second extends DefaultRegularSchedule:
  override def timeUntilNextRun(): FiniteDuration = FiniteDuration.apply(1, TimeUnit.SECONDS)

@Singleton
class TestNotifyConnector @Inject() (
  config: Configuration,
  auditService: AuditingService
)(using ec: ExecutionContext
) extends NotifyConnector(config, auditService):

  var transfer: List[VODataTransfer] = List[VODataTransfer]()
  var response: Try[Unit]            = Success(())

  override def sendEmailToVO(dataTransfer: VODataTransfer)(using hc: HeaderCarrier): Try[Unit] =
    println("dataTransfer: " + dataTransfer)
    transfer = dataTransfer :: transfer
    response
