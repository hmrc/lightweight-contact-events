package uk.gov.hmrc.lightweightcontactevents.infrastructure

import java.time.Clock
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.google.inject.AbstractModule
import javax.inject.{Inject, Singleton}
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lightweightcontactevents.DiAcceptanceTest
import uk.gov.hmrc.lightweightcontactevents.connectors.{AuditingService, VoaDataTransferConnector}
import uk.gov.hmrc.lightweightcontactevents.models.{ConfirmedContactDetails, PropertyAddress, QueuedDataTransfer, VOADataTransfer}
import uk.gov.hmrc.lightweightcontactevents.repository.QueuedDataTransferRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import uk.gov.hmrc.lightweightcontactevents.infrastructure._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.util.{Success, Try}

class ExportTransferSpec extends DiAcceptanceTest {
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

      implicit val actorSystem = app.actorSystem
      implicit val ec = app.injector.instanceOf[ExecutionContext]

      val probe = TestProbe("test-probe")
      actorSystem.eventStream.subscribe(probe.ref, classOf[ExportEvent]) //subscribe for event

      val transfer = aQueuedDataTransfer()
      await(repository().insert(transfer))  //item is in database, can trigger scheduler

      val scheduler = createScheduler()
      scheduler.start()

      val exportEvent = probe.expectMsgType[ExportEvent](3 seconds)

      val queueSize = await(repository().count)

      exportEvent must equal(ExportSucess)

      queueSize mustBe(0)

      exportConnector.transfer.head mustBe transfer.voaDataTransfer

    }

    "Keep items in DB if export fail" in {
      exportConnector.transfer = List.empty[VOADataTransfer]
      exportConnector.responseCode = 404
      implicit val actorSystem = app.actorSystem
      implicit val ec = app.injector.instanceOf[ExecutionContext]

      val probe = TestProbe("test-probe")
      actorSystem.eventStream.subscribe(probe.ref, classOf[ExportEvent]) //subscribe for event

      val transfer = aQueuedDataTransfer()
      await(repository().insert(transfer))  //item is in database, can trigger scheduler

      val scheduler = createScheduler()
      scheduler.start()

      val exportEvent = probe.expectMsgType[ExportEvent](3 seconds)

      val queueSize = await(repository().count)

      exportEvent must equal(ExportSucess)

      queueSize mustBe(1)
    }

  }


  def repository() = app.injector.instanceOf[QueuedDataTransferRepository]


  def createScheduler(): VoaDataTransferScheduler = {
    val actorSystem = app.injector.instanceOf[ActorSystem]
    implicit val ec = app.injector.instanceOf[ExecutionContext]
    new VoaDataTransferScheduler(
      actorSystem.scheduler,
      actorSystem.eventStream,
      new ScheduleEvery1Second(),
      app.injector.instanceOf[VoaDataTransferExporter],
      mongo,
      app.injector.instanceOf[VoaDataTransferLockKeeper]
    )
  }



  def aQueuedDataTransfer() = {
    QueuedDataTransfer(aVoaDataTransfer())
  }

  def aVoaDataTransfer() = {
    VOADataTransfer(aConfirmedContactDetails(), aPropertyAddress(), true,
      "Subject", "email@email.com", "category", "subCategory", "Free text message")
  }

  def aPropertyAddress() = {
    PropertyAddress("Some stree", None, "Some town", Some("Some county"), "BN12 4AX")
  }


  def aConfirmedContactDetails()  = {
    ConfirmedContactDetails(
      "John",
      "Doe",
      "email@noreply.voa.gov.uk",
      "0123456789"
    )
  }


}

class ScheduleEvery1Second extends DefaultRegularSchedule {
  override def timeUntilNextRun(): FiniteDuration = FiniteDuration.apply(1, TimeUnit.SECONDS)
}

@Singleton
class ExportTestDataTransferConnector @Inject() (http: HttpClient,
                                                  configuration: Configuration,
                                                  environment: Environment,
                                                  auditService:AuditingService)
  extends VoaDataTransferConnector(http, configuration, environment, auditService) {

  var transfer = List[VOADataTransfer]()

  var responseCode = 200

  override def transfer(dataTransfer: VOADataTransfer)(implicit hc: HeaderCarrier): Future[Try[Int]] = {
    transfer = dataTransfer :: transfer
    Future.successful(Success(responseCode))
  }
}
