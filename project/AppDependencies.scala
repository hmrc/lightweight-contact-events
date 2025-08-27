import play.core.PlayVersion
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.1.0"
  private val hmrcMongoVersion = "2.7.0"

  // Test dependencies
  private val scalaTestPlusPlayVersion = "7.0.2"
  private val scalaTestVersion         = "3.2.19"
  private val scalacheckVersion        = "3.2.19.0"
  private val mockitoVersion           = "3.2.19.0"
  private val scalaGuiceVersion        = "6.0.0" // Use 6.0.0 because 7.0.0 is not compatible with play-guice:3.0.8
  private val flexMarkVersion          = "0.64.8"

  private val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion
  )

  private val commonTests = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % Test,
    "org.playframework"      %% "play-test"          % PlayVersion.current      % Test,
    "org.scalatest"          %% "scalatest"          % scalaTestVersion         % Test,
    "com.vladsch.flexmark"    % "flexmark-all"       % flexMarkVersion          % Test // for scalatest 3.2.x
  )

  private val testOnly = Seq(
    "org.scalatestplus" %% "mockito-5-12" % mockitoVersion % Test
  )

  private val integrationTestOnly = Seq(
    "org.apache.pekko"  %% "pekko-testkit"   % PlayVersion.pekkoVersion % Test,
    "net.codingwell"    %% "scala-guice"     % scalaGuiceVersion        % Test,
    "org.scalatestplus" %% "scalacheck-1-18" % scalacheckVersion        % Test
  )

  val appDependencies: Seq[ModuleID] = compile ++ commonTests ++ testOnly

  val itDependencies: Seq[ModuleID] = commonTests ++ integrationTestOnly

}
