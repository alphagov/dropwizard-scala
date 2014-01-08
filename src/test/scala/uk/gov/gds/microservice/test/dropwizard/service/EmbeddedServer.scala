package uk.gov.gds.microservice.test.dropwizard.service

import com.yammer.dropwizard.config.Configuration
import com.yammer.dropwizard.Service

trait EmbeddedServer[T <: Configuration] {
  self: Service[T] =>

  val embeddedServerCommand: EmbeddedServerCommand[T] = new EmbeddedServerCommand[T](self)

  def startEmbeddedServer(configFileName: String) {
    run(Array[String]("embedded-server", configFileName))
  }

  def stopEmbeddedServer() {
    embeddedServerCommand.stop()
  }

  def isEmbeddedServerRunning: Boolean = {
    embeddedServerCommand.isRunning
  }
}

