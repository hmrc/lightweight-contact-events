import play.sbt.PlayImport.PlayKeys
import uk.gov.hmrc.DefaultBuildSettings.itSettings

val appName = "lightweight-contact-events"

ThisBuild / scalaVersion := "3.7.2"
ThisBuild / majorVersion := 1

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    PlayKeys.playDefaultPort := 7312,
    libraryDependencies ++= AppDependencies.appDependencies,
    maintainer := "voa.service.optimisation@digital.hmrc.gov.uk",
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:msg=Flag .* set repeatedly:s",
    scalacOptions += "-Wconf:msg=Implicit parameters should be provided with a \\`using\\` clause&src=views/.*:s",
    scalacOptions += "-feature",
    javaOptions += "-XX:+EnableDynamicAgentLoading",
    Test / parallelExecution := false
  )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice)
  .settings(itSettings())
  .settings(
    scalacOptions += "-Wconf:msg=Flag .* set repeatedly:s",
    libraryDependencies ++= AppDependencies.itDependencies
  )

addCommandAlias("scalastyle", ";scalafmtAll;scalafmtSbt;it/test:scalafmt;scalafixAll")
