import sbt.Keys._
import sbt._
import play.sbt.PlayImport.PlayKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "lightweight-contact-events"

lazy val root = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
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
    scalaVersion := "2.13.8",
    DefaultBuildSettings.targetJvm := "jvm-11",
    maintainer := "voa.service.optimisation@digital.hmrc.gov.uk"
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo
  ))
