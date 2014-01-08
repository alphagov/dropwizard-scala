package uk.gov.gds.microservice.swagger

import com.yammer.dropwizard.assets.AssetsBundle
import com.yammer.dropwizard.config.Bootstrap
import com.yammer.dropwizard.config.Environment
import com.wordnik.swagger.jaxrs.JaxrsApiReader

class SwaggerBundle extends AssetsBundle("/swagger-ui", "/api-docs/swagger-ui") {

  override def run(environment: Environment) {
    JaxrsApiReader.setFormatString("")
    environment.addResource(new SwaggerApiResource)

    super.run(environment)
  }

  override def initialize(bootstrap: Bootstrap[_]) {
    bootstrap.addBundle(new AssetsBundle("/api-docs"))
    super.initialize(bootstrap)
  }
}