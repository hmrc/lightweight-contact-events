import play.core.PlayVersion
import sbt._

object Dependencies {

  private val bootstrapVersion = "7.11.0"
  private val hmrcMongoVersion = "0.73.0"

  // Test dependencies
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val scalaTestVersion = "3.2.14"
  private val scalacheckVersion = "3.2.14.0"
  private val mockitoVersion = "3.2.14.0"
  private val scalaGuiceVersion = "5.1.0"
  private val flexMarkVersion = "0.64.0"

  private val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % hmrcMongoVersion
  )

  private val commonTestScope: String = "test,it"

  private val commonTest = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % commonTestScope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % commonTestScope,
    "org.scalatest" %% "scalatest" % scalaTestVersion % commonTestScope,
    "org.scalatestplus" %% "mockito-4-6" % mockitoVersion % commonTestScope,
    "com.vladsch.flexmark" % "flexmark-all" % flexMarkVersion % commonTestScope // for scalatest 3.2.x
  )

  private val integrationTest = Seq(
    "com.typesafe.akka" %% "akka-testkit" % PlayVersion.akkaVersion % IntegrationTest,
    "net.codingwell" %% "scala-guice" % scalaGuiceVersion % IntegrationTest,
    "org.scalatestplus" %% "scalacheck-1-17" % scalacheckVersion % IntegrationTest
  )

  val appDependencies: Seq[ModuleID] = compile ++ commonTest ++ integrationTest

}
