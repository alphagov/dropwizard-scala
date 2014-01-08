package uk.gov.gds.microservice.rest

import uk.gov.gds.microservice.dropwizard.conf._
import com.yammer.metrics.core.HealthCheck
import uk.gov.gds.microservice.test.dropwizard.service.{ MicroServiceEmbeddedServer, EmbeddedServer, ServiceSpec }
import uk.gov.gds.microservice.test.{ Port, BaseSpec }
import uk.gov.gds.microservice.test.http.ExtractJson
import com.yammer.dropwizard.config.{ Environment, Bootstrap }

case class TestAppConfiguration() extends BaseConfiguration with ServiceMappings

abstract class TestAppMicroService(val resources: List[Any], val hcs: Option[List[HealthCheck]] = None) extends MicroService[TestAppConfiguration] with ServiceClients[TestAppConfiguration] {

  val applicationName: String = "testapp"

  override def run(configuration: TestAppConfiguration, environment: Environment) {
    super.run(configuration, environment)

    val hc = makeHttpClient(configuration, environment)

    managed(environment, new ManagedHttpClient(hc))

    resources(environment, resources)

    if (hcs.isDefined) healthChecks(environment, hcs.get)
  }
}

trait TestAppMicroServiceSpec extends ServiceSpec[TestAppConfiguration, TestAppMicroService] with ExtractJson {

  val configPath: String = "/uk/gov/gds/microservice/rest/test-app-conf.json"

  val resources: List[Any]

  val healthChecks: Option[List[HealthCheck]] = None

  lazy val applicationService: TestAppMicroService = new TestAppMicroService(resources, healthChecks) with EmbeddedServer[TestAppConfiguration] {

    override def initialize(userConfigurationBootstrap: Bootstrap[TestAppConfiguration]) {
      userConfigurationBootstrap.addCommand(embeddedServerCommand)
      super.initialize(userConfigurationBootstrap)
    }
  }
}
