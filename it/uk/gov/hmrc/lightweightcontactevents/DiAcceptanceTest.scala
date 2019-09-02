package uk.gov.hmrc.lightweightcontactevents

import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import play.modules.reactivemongo.{ReactiveMongoComponent, ReactiveMongoHmrcModule}
import reactivemongo.api.MongoConnection
import scala.concurrent.ExecutionContext.Implicits.global

trait DiAcceptanceTest extends WordSpecLike with BeforeAndAfterAll with MustMatchers with FutureAwaits
  with DefaultAwaitTimeout with GuiceOneAppPerSuite {

  def testDbPrefix(): String

  implicit lazy val conn: MongoConnection = app.injector.instanceOf[ReactiveMongoComponent].mongoConnector.helper.connection

  def fakeApplicationBuilder(): GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .configure(customConfigs)
    .bindings(new ReactiveMongoHmrcModule)

  def testDbName = s"$testDbPrefix${java.util.UUID.randomUUID.toString.replaceAll("-", "")}"
  final val testDbUri = s"mongodb://localhost:27017/$testDbName?rm.tcpNoDelay=true&rm.nbChannelsPerNode=3&writeConcern=unacknowledged"

  def customConfigs: Map[String, Any] = Map(
    "mongodb.uri" -> testDbUri
  )

  def mongo = app.injector.instanceOf[ReactiveMongoComponent]

  override final def fakeApplication(): Application = fakeApplicationBuilder().build()

  override protected def afterAll(): Unit = {
    await(mongo.mongoConnector.db().drop())
  }

}

