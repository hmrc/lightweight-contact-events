import play.core.PlayVersion
import sbt._

object Dependencies {

  private val bootstrapVersion = "5.23.0"
  private val hmrcMongoVersion = "0.63.0"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val scalaTestVersion = "3.2.11"
  private val scalacheckVersion = "3.2.11.0"
  private val mockitoVersion = "3.2.11.0"
  private val mockitoAllVersion = "1.10.19"
  private val scalaGuiceVersion = "5.0.2"
  private val flexmarkVersion = "0.62.2"

  private val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % hmrcMongoVersion
  )

  private val commonTestScope: String = "test,it"

  private val commonTest = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % commonTestScope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % commonTestScope,
    "org.scalatest" %% "scalatest" % scalaTestVersion % commonTestScope,
    "org.scalatestplus" %% "mockito-4-2" % mockitoVersion % commonTestScope,
    "org.mockito" % "mockito-all" % mockitoAllVersion % commonTestScope,
    "com.vladsch.flexmark" % "flexmark-all" % flexmarkVersion % commonTestScope // for scalatest 3.2.x
  )

  private val integrationTest = Seq(
    "com.typesafe.akka" %% "akka-testkit" % PlayVersion.akkaVersion % IntegrationTest,
    "net.codingwell" %% "scala-guice" % scalaGuiceVersion % IntegrationTest,
    "org.scalatestplus" %% "scalacheck-1-15" % scalacheckVersion % IntegrationTest
  )

  val appDependencies: Seq[ModuleID] = compile ++ commonTest ++ integrationTest

}
