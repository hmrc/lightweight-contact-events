import play.core.PlayVersion
import sbt.*

object AppDependencies {

  private val bootstrapVersion   = "10.8.0"
  private val voServiceVersion   = "0.12.0"
  private val hmrcMongoVersion   = "2.13.0"
  private val govukNotifyVersion = "6.2.0-RELEASE"

  // Test dependencies
  private val voTestVersion = "0.6.0"

  private val compile = Seq(
    "uk.gov.hmrc"          %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"          %% "vo-backend-service"        % voServiceVersion,
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "uk.gov.service.notify" % "notifications-java-client" % govukNotifyVersion
  )

  private val commonTests = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
  )

  private val testOnly = Seq(
    "uk.gov.hmrc" %% "vo-unit-test" % voTestVersion % Test
  )

  private val integrationTestOnly = Seq(
    "uk.gov.hmrc"       %% "vo-integration-test"     % voTestVersion            % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion         % Test,
    "org.apache.pekko"  %% "pekko-testkit"           % PlayVersion.pekkoVersion % Test
  )

  val appDependencies: Seq[ModuleID] = compile ++ commonTests ++ testOnly

  val itDependencies: Seq[ModuleID] = commonTests ++ integrationTestOnly

}
