import play.core.PlayVersion
import sbt.*

object Dependencies {

  private val bootstrapVersion = "8.2.0"
  private val hmrcMongoVersion = "1.6.0"

  // Test dependencies
  private val scalaTestPlusPlayVersion = "7.0.0"
  private val scalaTestVersion = "3.2.17"
  private val scalacheckVersion = "3.2.17.0"
  private val mockitoVersion = "3.2.17.0"
  private val scalaGuiceVersion = "6.0.0"
  private val flexMarkVersion = "0.64.8"

  private val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion
  )

  private val commonTestScope: String = "test,it"

  private val commonTest = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play"   % scalaTestPlusPlayVersion % commonTestScope,
    "org.playframework"      %% "play-test"            % PlayVersion.current % commonTestScope,
    "org.scalatest"          %% "scalatest"            % scalaTestVersion % commonTestScope,
    "org.scalatestplus"      %% "mockito-4-11"         % mockitoVersion % commonTestScope,
    "com.vladsch.flexmark"   % "flexmark-all"          % flexMarkVersion % commonTestScope // for scalatest 3.2.x
  )

  private val integrationTest = Seq(
    "org.apache.pekko"       %% "pekko-testkit"        % PlayVersion.pekkoVersion % IntegrationTest,
    "net.codingwell"         %% "scala-guice"          % scalaGuiceVersion % IntegrationTest,
    "org.scalatestplus"      %% "scalacheck-1-17"      % scalacheckVersion % IntegrationTest
  )

  val appDependencies: Seq[ModuleID] = compile ++ commonTest ++ integrationTest

}
