import sbt.Keys._
import sbt._
import play.sbt.PlayImport.PlayKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import play.sbt.routes.RoutesKeys
RoutesKeys.routesImport := Seq.empty

val appName = "lightweight-contact-events"

lazy val root = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin): _*)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;" +
      ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;.*DataCacheConnector;" +
      ".*ControllerConfiguration;.*LanguageSwitchController;.*Repository;",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false,
    PlayKeys.playDefaultPort := 7312
  )
  .settings(majorVersion := 1)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    libraryDependencies ++= Dependencies.appDependencies,
    retrieveManaged := true,
    scalaVersion := "2.13.8"
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo
  ))

// Remove `excludeDependencies` when Play framework and sbt-scoverage plugin released for Scala 3

//excludeDependencies ++= Seq(
//  "com.typesafe.play" % "play-server_3",
//  "com.typesafe.play" % "play-akka-http-server_3",
//  "com.typesafe.play" % "play-logback_3",
//  "com.typesafe.play" % "filters-helpers_3",
//  "com.typesafe.play" % "twirl-api_3",
//  "com.typesafe.play" % "play-test_3",
//  "com.typesafe.play" % "play-docs_3",
//  "org.scala-lang.modules" % "scala-xml_3",
//  "org.scoverage" % "scalac-scoverage-runtime_3",
//  "org.scoverage" % "scalac-scoverage-plugin_3.1.1",
//  "com.typesafe.play" % "play-functional_2.13",
//  "com.typesafe.play" % "play-json_2.13",
//  "com.fasterxml.jackson.module" % "jackson-module-scala_2.13",
//  "org.scala-lang.modules" % "scala-java8-compat_2.13",
//  "org.scalatest" % "scalatest_2.13",
//  "org.scalactic" % "scalactic_2.13"
//)
