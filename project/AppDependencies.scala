import play.core.PlayVersion
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.7.0"
  private val hmrcMongoVersion = "2.12.0"

  // Test dependencies
  private val scalacheckVersion = "3.2.19.0"
  private val mockitoVersion    = "3.2.19.0"
  private val scalaGuiceVersion = "6.0.0" // Use 6.0.0 because 7.0.0 is not compatible with play-guice:3.0.10

  private val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion
  )

  private val commonTests = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test
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
