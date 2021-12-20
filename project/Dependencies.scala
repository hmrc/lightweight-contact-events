import sbt._

object Dependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val scalaTestVersion = "3.2.9"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val pegdownVersion = "1.6.0"
  private val mockitoAllVersion = "1.10.19"
  private val bootstrapVersion = "5.16.0"
  private val simpleReactivemongoVersion = "8.0.0-play-28"
  private val hmrcMongoLock = "7.0.0-play-28"
  private val akkaVersion = PlayVersion.akkaVersion
  private val flexmarkVersion = "0.35.10"
  private val mockitoVersion = "3.2.9.0"
  private val jsoupVersion = "1.14.3"
  private val scalacheckVersion = "3.2.9.0"
  private val scalaGuiceVersion = "5.0.2"

  lazy val appDependencies: Seq[ModuleID] = compile ++ Test() ++ IntegrationTest() ++ tmpMacWorkaround()

  val compile = Seq(
    ws,
    guice,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "simple-reactivemongo" % simpleReactivemongoVersion,
    "uk.gov.hmrc" %% "mongo-lock" % hmrcMongoLock,
    "com.typesafe.akka" %% "akka-actor"  % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"  % akkaVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "com.vladsch.flexmark" % "flexmark-all" % flexmarkVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.scalatestplus" %% "mockito-3-4" % mockitoVersion % "test",
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
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
        "org.scalatestplus" %% "mockito-3-4" % mockitoVersion % scope,
        "org.scalatestplus" %% "scalacheck-1-15" % scalacheckVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % scope,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % scope,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % scope,
        "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion % scope,
        "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion % scope,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion % scope,
        "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion % scope,
        "net.codingwell" %% "scala-guice" % scalaGuiceVersion % scope
      )
    }.test
  }

  def tmpMacWorkaround(): Seq[ModuleID] =
    if (sys.props.get("os.name").fold(false)(_.toLowerCase.contains("mac")))
      Seq("org.reactivemongo" % "reactivemongo-shaded-native" % "0.18.8-osx-x86-64" % "runtime,test,it")
    else Seq()

}
