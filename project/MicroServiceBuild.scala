import sbt._

object MicroServiceBuild extends Build with MicroService {

  val appName = "lightweight-contact-events"

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val hmrcTestVersion = "3.4.0-play-25"
  private val scalaTestVersion = "3.0.4"
  private val scalaTestPlusPlayVersion = "2.0.1"
  private val pegdownVersion = "1.6.0"
  private val mockitoAllVersion = "1.10.19"
  private val bootstrapVersion = "4.8.0"
  private val reactiveMongoVersion = "6.2.0"


  override lazy val appDependencies: Seq[ModuleID] = compile ++ Test()

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-reactivemongo" % reactiveMongoVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.10.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-all" % mockitoAllVersion % scope
      )
    }.test
  }

}
