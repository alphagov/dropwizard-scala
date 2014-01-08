package uk.gov.gds.microservice.dropwizard.rest

import com.yammer.metrics.core.HealthCheck
import com.yammer.metrics.core.HealthCheck.Result
import com.yammer.dropwizard.config.Configuration
import org.json4s._
import org.json4s.jackson.JsonMethods._

class ConfigHealthCheck(conf: Configuration, confHealthCheckName: String = "conf-healthcheck")(implicit formats: Formats) extends HealthCheck(confHealthCheckName) {

  lazy val jsonConf = pretty(Extraction.decompose(conf))

  def check(): Result = Result.healthy(jsonConf)
}
