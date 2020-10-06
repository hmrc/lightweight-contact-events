import sbt._


object Dependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val scalaTestVersion = "3.0.8"
  private val scalaTestPlusPlayVersion = "3.1.3"
  private val pegdownVersion = "1.6.0"
  private val mockitoAllVersion = "1.10.19"
  private val bootstrapVersion = "1.16.0"
  private val simpleReactivemongoVersion = "7.30.0-play-26"
  private val hmrcMongoLock = "6.21.0-play-26"
  private val akkaVersion = "2.5.23"

  lazy val appDependencies: Seq[ModuleID] = compile ++ Test() ++ IntegrationTest() ++ tmpMacWorkaround()

  val compile = Seq(
    ws,
    guice,
    "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion,
    "uk.gov.hmrc" %% "mongo-lock" % hmrcMongoLock,
    "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"  % akkaVersion
  )

  lazy val appDependencyOverrides: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-stream"    % akkaVersion     force(),
    "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion     force(),
    "com.typesafe.akka" %% "akka-actor"     % akkaVersion     force(),
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.10.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockitoAllVersion % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % scope,
        "net.codingwell" %% "scala-guice" % "4.2.6" % scope
      )
    }.test
  }

  def tmpMacWorkaround(): Seq[ModuleID] =
    if (sys.props.get("os.name").fold(false)(_.toLowerCase.contains("mac")))
      Seq("org.reactivemongo" % "reactivemongo-shaded-native" % "0.18.8-osx-x86-64" % "runtime,test,it")
    else Seq()

}
