import play.core.PlayVersion
import sbt._

object Dependencies {


  private val bootstrapVersion = "5.22.0"
  private val hmrcMongoVersion = "0.62.0"
  private val playJsonVersion = "2.10.0-RC6"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val scalaTestVersion = "3.2.11"
  private val scalacheckVersion = "3.2.11.0"
  private val mockitoVersion = "3.2.11.0"
  private val mockitoAllVersion = "1.10.19"
  private val scalaGuiceVersion = "5.0.2"
  private val flexmarkVersion = "0.62.2"

  private val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % hmrcMongoVersion,
  ).map(_.cross(CrossVersion.for3Use2_13)) ++ Seq(
    "com.typesafe.play" %% "play-json" % playJsonVersion,
  )

  private val commonScope: String = "test,it"

  private val commonTest = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % commonScope
  ).map(_.cross(CrossVersion.for3Use2_13)) ++ Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % commonScope,
    "org.scalatestplus" %% "mockito-4-2" % mockitoVersion % commonScope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % commonScope,
    "org.mockito" % "mockito-all" % mockitoAllVersion % commonScope,
    "com.vladsch.flexmark" % "flexmark-all" % flexmarkVersion % commonScope // for scalatest 3.2.x
  )

  private val integrationTest = Seq(
    "net.codingwell" %% "scala-guice" % scalaGuiceVersion % IntegrationTest
  ).map(_.cross(CrossVersion.for3Use2_13)) ++ Seq(
    "org.scalatestplus" %% "scalacheck-1-15" % scalacheckVersion % IntegrationTest
  )

  val appDependencies: Seq[ModuleID] = compile ++ commonTest ++ integrationTest

}
