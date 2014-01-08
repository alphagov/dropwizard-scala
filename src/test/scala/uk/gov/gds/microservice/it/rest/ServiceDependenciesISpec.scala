package uk.gov.gds.microservice.it.rest

import com.yammer.metrics.core.HealthCheck
import uk.gov.gds.microservice.test.dropwizard.service.MicroServiceEmbeddedServer
import uk.gov.gds.microservice.rest.{ ErrorHandlingTestHealthCheck, ErrorHandlingTestResource, AnotherAppMicroServiceIntegration, TestAppMicroServiceSpec }

/**
 * NOTE: this should be an integration test but because it requires items from the test package compilation is failing
 */
class ServiceDependenciesISpec extends TestAppMicroServiceSpec {
  lazy val anotherService = new AnotherAppMicroServiceIntegration

  override lazy val dependentServices: Option[Map[String, MicroServiceEmbeddedServer[_, _]]] = Some(Map("anotherService" -> anotherService))

  val resources: List[Any] = List(new ErrorHandlingTestResource)

  override val healthChecks: Option[List[HealthCheck]] = Some(List(new ErrorHandlingTestHealthCheck))

  "Two dependant micoservice" should {
    "have the same port configuration" in {
      anotherService.config.path must not be anotherService.configPath
      debug(s"Path to dependent service config : ${anotherService.config.path}")

      config.path must not be configPath
      debug(s"Path to service under test config : ${config.path}")
    }
  }

}
