import play.sbt.PlayImport.PlayKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, itSettings, scalaSettings}

val appName = "lightweight-contact-events"

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always // Resolves versions conflict

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.12"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;" +
      ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;.*DataCacheConnector;" +
      ".*ControllerConfiguration;.*LanguageSwitchController;.*Repository;",
    ScoverageKeys.coverageMinimumStmtTotal := 98,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
  )
  .settings(scalaSettings)
  .settings(defaultSettings())
  .settings(
    PlayKeys.playDefaultPort := 7312,
    libraryDependencies ++= AppDependencies.appDependencies,
    scalacOptions += "-Wconf:src=routes/.*:s",
    maintainer := "voa.service.optimisation@digital.hmrc.gov.uk",
    Test / parallelExecution := false
  )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice)
  .settings(itSettings)
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
