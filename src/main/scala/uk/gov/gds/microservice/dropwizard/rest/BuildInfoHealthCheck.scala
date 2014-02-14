package uk.gov.gds.microservice.dropwizard.rest

import com.yammer.metrics.core.HealthCheck
import com.yammer.metrics.core.HealthCheck.Result

class BuildInfoHealthCheck(p: Package) extends HealthCheck("build-info") {

  val implementationVersion =  Option(p.getImplementationVersion)
  val buildNumber = implementationVersion.filterNot(_.isEmpty).getOrElse("[not available]")

  def check() = {
    Result.healthy(s"build-number: $buildNumber")
  }

}
