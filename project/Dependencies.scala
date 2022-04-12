import sbt._

object Dependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val bootstrapVersion = "5.22.0"
  private val hmrcMongoVersion = "0.62.0"
  private val akkaVersion = PlayVersion.akkaVersion
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val scalaTestVersion = "3.2.11"
  private val scalacheckVersion = "3.2.11.0"
  private val mockitoVersion = "3.2.11.0"
  private val mockitoAllVersion = "1.10.19"
  private val scalaGuiceVersion = "5.0.2"
  private val flexmarkVersion = "0.62.2"

  private val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % hmrcMongoVersion,
  )

  private val commonScope: String = "test,it"

  private val commonTest = Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % commonScope,
    "com.vladsch.flexmark" % "flexmark-all" % flexmarkVersion % commonScope, // for scalatest 3.2.x
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % commonScope,
    "org.scalatestplus" %% "mockito-4-2" % mockitoVersion % commonScope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % commonScope,
    "org.mockito" % "mockito-all" % mockitoAllVersion % commonScope
  )

  private val integrationTest = Seq(
    "org.scalatestplus" %% "scalacheck-1-15" % scalacheckVersion % IntegrationTest,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % IntegrationTest,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % IntegrationTest,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % IntegrationTest,
    "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion % IntegrationTest,
    "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion % IntegrationTest,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion % IntegrationTest,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion % IntegrationTest,
    "net.codingwell" %% "scala-guice" % scalaGuiceVersion % IntegrationTest
  )

  val appDependencies: Seq[ModuleID] = compile ++ commonTest ++ integrationTest

}
