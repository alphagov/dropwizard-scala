package uk.gov.gds.microservice.rest

import javax.ws.rs._
import scala.Array
import uk.gov.gds.microservice.dropwizard.rest.Resource
import javax.ws.rs.core.Response
import com.yammer.metrics.core.HealthCheck
import com.yammer.metrics.core.HealthCheck.Result
import uk.gov.gds.microservice.dropwizard.conf._
import com.yammer.dropwizard.config.{ Bootstrap, Environment }
import uk.gov.gds.microservice.test.dropwizard.service.{ EmbeddedServer, MicroServiceEmbeddedServer }

@Path("/another")
@Consumes(Array(Resource.ApplicationJson))
@Produces(Array(Resource.ApplicationJson))
class AnotherTestResource {

  import Resource._

  @GET
  @Path("/ping")
  def ping: Response = Ok

}

class AnotherTestHealthCheck extends HealthCheck("AnotherTestResource") {

  def check() = Result.healthy()
}

case class AnotherAppConfiguration() extends BaseConfiguration with ServiceMappings

abstract class AnotherAppMicroService extends MicroService[AnotherAppConfiguration] with ServiceClients[AnotherAppConfiguration] {

  val applicationName: String = "anotherapp"

  override def run(configuration: AnotherAppConfiguration, environment: Environment) {
    super.run(configuration, environment)

    val hc = makeHttpClient(configuration, environment)

    managed(environment, new ManagedHttpClient(hc))

    resources(environment, List(new AnotherTestResource))
    healthChecks(environment, List(new AnotherTestHealthCheck))
  }
}

class AnotherAppMicroServiceIntegration extends MicroServiceEmbeddedServer[AnotherAppConfiguration, AnotherAppMicroService] {
  val applicationService = new AnotherAppMicroService with EmbeddedServer[AnotherAppConfiguration] {

    override def initialize(userConfigurationBootstrap: Bootstrap[AnotherAppConfiguration]) {
      userConfigurationBootstrap.addCommand(embeddedServerCommand)
      super.initialize(userConfigurationBootstrap)
    }
  }

  val configPath: String = "/uk/gov/gds/microservice/rest/another-app-conf.json"
}
