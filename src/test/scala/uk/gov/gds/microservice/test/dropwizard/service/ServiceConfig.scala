package uk.gov.gds.microservice.test.dropwizard.service

import uk.gov.gds.microservice.utils.Logging
import com.yammer.dropwizard.config.Configuration
import com.yammer.dropwizard.Service

case class ServiceConfig(configPath: String, servicePort: Int, adminPort: Int) extends Logging {

  import scala.io.Source
  import java.io.{ File, FileWriter }
  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  private val configJson = Source.fromInputStream(getClass.getResourceAsStream(configPath)).mkString
  private val testFileName = s"test-${configPath.split("/").last.replace(".json", "")}"
  private var json = parse(StringInput(configJson))

  val defaultServicePort: Int = (json \ "http" \\ "port").asInstanceOf[JInt].values.intValue()

  json = json replace (List("http", "port"), JInt(servicePort))
  json = json replace (List("http", "adminPort"), JInt(adminPort))

  def transformServicePort(from: Int, to: Int) {
    json = json transformField {
      case ("port", JInt(p)) if p == from => ("port", JInt(to))
    }
  }

  def replaceServiceMappingsPort(mapppedPorts: Option[Map[String, Int]]) {
    mapppedPorts match {
      case Some(mapping) => mapping.foreach(m => {
        debug(s"Replacing configured service keyed by ${m._1} with port ${m._2}")
        replaceServiceMappingsPort(m._1, m._2)
      })
      case _ =>
    }
  }

  def replaceServiceMappingsPort(serviceKey: String, port: Int) {
    json = json replace (List("serviceMappings", serviceKey, "port"), JInt(port))
  }

  def mongoHost: String = (json \ "mongodb" \\ "hosts").asInstanceOf[JString].values

  def mongoDbName: String = (json \ "mongodb" \\ "db").asInstanceOf[JString].values

  def mongoDbName(to: String) {
    json = json replace (List("mongodb", "db"), JString(to))
  }

  lazy val path: String = {
    val tmp = File.createTempFile(testFileName, ".json")
    val fw = new FileWriter(tmp)
    fw.write(pretty(json))
    fw.close()
    tmp.getAbsolutePath
  }
}

object ServiceConfig extends Logging {

  def apply(configPath: String, dbNameForTest: Option[String], servicePort: Int, adminPort: Int, dependentServicesPortMapping: Option[Map[String, Int]] = None): ServiceConfig = {
    debug(s"Configuring service (${this.getClass.getSimpleName}) with port: $servicePort and admin port : $adminPort")

    val sc = new ServiceConfig(configPath, servicePort, adminPort)
    sc.transformServicePort(sc.defaultServicePort, sc.servicePort)

    sc.replaceServiceMappingsPort(dependentServicesPortMapping)

    dbNameForTest match {
      case Some(dbName) =>
        debug(s"Starting service with a test-specific Mongo database name: $dbNameForTest")
        sc.mongoDbName(dbName)
      case _ => debug("Starting service with its configured Mongo database name: " + sc.mongoDbName)
    }

    sc
  }
}

trait MongoSupport {

  import uk.gov.gds.microservice.mongo.MongoConnector

  val dbNameForTest: Option[String] = Some(s"test-${this.getClass.getSimpleName}")

  lazy val testMongoConnector = new MongoConnector(s"mongodb://127.0.0.1:27017/${dbNameForTest.get}?maxPoolSize=20&waitqueuemultiple=10")
}

trait MicroServiceEmbeddedServer[C <: Configuration, S <: Service[C]] extends MongoSupport with Logging {

  import uk.gov.gds.microservice.test.Port

  val applicationService: S
  val configPath: String
  lazy val servicePort: Int = Port.randomAvailable
  lazy val adminPort: Int = Port.randomAvailable
  //NOTE: if your test requires dependent services then it must be run in sequential execution. Integration test phase is the most appropriate use
  lazy val dependentServices: Option[Map[String, MicroServiceEmbeddedServer[_, _]]] = None

  lazy val dependentServicesPortMapping = dependentServices match {
    case Some(es) => Some(es.map(embedded => embedded._1 -> embedded._2.config.servicePort))
    case _ => None
  }

  lazy val config: ServiceConfig = ServiceConfig(configPath, dbNameForTest, servicePort, adminPort, dependentServicesPortMapping)

  lazy val embeddedServer = applicationService.asInstanceOf[EmbeddedServer[C]]
  lazy val start = startServer _

  def resource(url: String): String = s"http://localhost:${config.servicePort}/${-/(url)}"

  def stop() {
    safelyStop(embeddedServer.stopEmbeddedServer())
    stopExternalServices()
  }

  private def startServer(fail: => Nothing): EmbeddedServer[C] = {
    startExternalServices(fail)

    debug(s"Starting service with config path : ${config.path}")
    embeddedServer.startEmbeddedServer(config.path)

    if (!embeddedServer.isEmbeddedServerRunning) fail

    embeddedServer
  }

  private def startExternalServices(fail: => Nothing) {
    dependentServices match {
      case Some(es) => es.values.foreach(embeddedServer => embeddedServer.start(fail))
      case _ =>
    }
  }

  private def stopExternalServices() {
    dependentServices match {
      case Some(es) => es.values.foreach(embeddedServer => safelyStop(embeddedServer.stop()))
      case _ =>
    }
  }

  private def -/(uri: String) = if (uri.startsWith("/")) uri.drop(1) else uri

  private def safelyStop[T](action: => T) {
    try {
      action
    } catch {
      case t: Throwable => error("An exception occurred while stopping an embedded server", t)
    }
  }
}

