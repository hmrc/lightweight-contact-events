import play.sbt.PlayImport.PlayKeys
import uk.gov.hmrc.DefaultBuildSettings.itSettings

val appName = "lightweight-contact-events"

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always // Resolves versions conflict

ThisBuild / scalaVersion := "3.4.2"
ThisBuild / majorVersion := 1

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
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
  .settings(itSettings())
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
