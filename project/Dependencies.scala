import sbt._
import sbt.Keys._
import scala.Some

object Version {
  val thisApp = "1.0.0-SNAPSHOT"
  val scala = "2.10.2"
  val dropWizard = "0.6.2"
  val json4s = "3.2.4"
  val salat = "1.9.2"
}

object Settings {
  val commonSettings = Defaults.defaultSettings ++
    Seq(
      organization := "uk.gov.gds",
      version := Version.thisApp,
      scalaVersion := Version.scala,
      scalacOptions ++= Seq(
        "-unchecked",
        "-deprecation",
        "-Xlint",
        "-language:_",
        "-target:jvm-1.7",
        "-encoding", "UTF-8"
      ),
      resolvers ++= Repositories.resolvers,
      initialCommands in console := "import uk.gov.gds.microservice._",
      parallelExecution in Test := false,
      publishArtifact := true,
      publishArtifact in Test := true,
      credentials += Publishing.credentials, 
      publishTo := Publishing.publishTo(version.value)
    )
}

object Publishing {
  lazy val credentials = Credentials(Path.userHome / ".ivy2" / ".credentials")

  def publishTo(version: String) = {
    val host = "https://oss.jfrog.org/"
    if (version.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at host + "oss-snapshot-local")
    else
      Some("releases"  at host + "oss-release-local")
  } 
}

object Dependencies {

  import Version._

  object Compile {
    val nscalaTime = "com.github.nscala-time" %% "nscala-time" % "0.4.0"
    val json4sJackson = "org.json4s" %% "json4s-jackson" % json4s
    val dropWizardClient = "com.yammer.dropwizard" % "dropwizard-client" % dropWizard
    val dropWizardScala = "com.massrelevance" %% "dropwizard-scala" % "0.6.2-1"

    val casbah = "org.mongodb" %% "casbah-core" % "2.6.1"
    val mongoJavaDriver = "org.mongodb" % "mongo-java-driver" % "2.11.1"
    val salatCore = "com.novus" %% "salat-core" % salat
    val salatUtil = "com.novus" %% "salat-util" % salat

    val swagger = "com.wordnik" % "swagger-jaxrs_2.10.0" % "1.2.5" excludeAll( ExclusionRule(organization = "javax.ws.rs", name="jsr311-api"), ExclusionRule(organization = "com.sun.jersey"), ExclusionRule(organization = "com.fasterxml.jackson.module"))
    val dropwizardSwagger = "io.tesla.dropwizard" % "dropwizard-swagger" % "0.1.1" excludeAll(
      ExclusionRule(organization = "javax.ws.rs", name="jsr311-api"),
      ExclusionRule(organization = "com.sun.jersey"),
      ExclusionRule(organization = "com.fasterxml.jackson.module"),
      ExclusionRule(organization = "com.wordnik", name = "swagger-core_2.9.1"),
      ExclusionRule(organization = "com.wordnik", name = "swagger-jaxrs_2.9.1"),
      ExclusionRule(organization = "com.wordnik", name = "swagger-annotations_2.9.1"))
  }

  sealed abstract class Test(scope: String) {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.0.M5b" % scope
    val junit = "junit" % "junit" % "4.11" % scope
    val mockito = "org.mockito" % "mockito-all" % "1.9.5" % scope
    val dropWizardTest = "com.yammer.dropwizard" % "dropwizard-testing" % dropWizard % scope
    val httpClient = "org.apache.httpcomponents" % "httpclient" % "4.2.2" % scope
  }

  object Test extends Test("test")

  object IntegrationTest extends Test("it")

  val commonDependencies = Seq(
    Compile.nscalaTime,
    Compile.json4sJackson,
    Compile.dropWizardClient,
    Compile.dropWizardScala,
    Compile.casbah,
    Compile.mongoJavaDriver,
    Compile.salatCore,
    Compile.salatUtil,
    Compile.swagger,
    Compile.dropwizardSwagger,

    Test.scalaTest,
    Test.junit,
    Test.mockito,
    Test.dropWizardTest,
    Test.httpClient
  )

}

object Repositories {

  import sbt.Keys._

  val resolvers = Seq(
    Resolver.mavenLocal,
    Opts.resolver.sonatypeReleases,
    Opts.resolver.sonatypeSnapshots,
    "federico-snapshots" at "https://repository-federecio1.forge.cloudbees.com/snapshot/"
  )
}

