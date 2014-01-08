package uk.gov.gds.microservice.dropwizard.conf

import com.yammer.dropwizard.config.Configuration
import com.fasterxml.jackson.annotation.{ JsonIgnore, JsonProperty }
import javax.validation.constraints.NotNull
import org.hibernate.validator.constraints.NotEmpty
import javax.validation.Valid
import com.yammer.dropwizard.client.JerseyClientConfiguration

trait MongoConfiguration {

  @JsonProperty
  @NotEmpty
  val hosts: String = "NOT CONFIGURED"

  @JsonProperty
  @NotEmpty
  val db: String = "NOT CONFIGURED"

  @JsonProperty
  @NotEmpty
  val options: String = "NOT CONFIGURED"
}

sealed case class MongoConf() extends Configuration with MongoConfiguration {
  def toURL = s"mongodb://$hosts/$db?$options"
}

trait ClientConfiguration {

  @Valid
  @NotNull
  @JsonProperty
  val httpClient = new HttpsClientConfiguration
}

class HttpsClientConfiguration extends JerseyClientConfiguration {

  @JsonProperty
  val sslPeersValidation: Boolean = true
}

trait EnvironmentConfiguration {

  @NotNull
  @JsonProperty
  val environment: String = "NOT CONFIGURED"
}

class ServiceConfiguration() extends Configuration {
  @JsonProperty
  @NotEmpty
  val protocol: String = "NOT CONFIGURED"

  @JsonProperty
  @NotEmpty
  val host: String = "NOT CONFIGURED"

  @JsonProperty
  @NotEmpty
  val port: Int = -1

  @JsonProperty
  val path: String = ""

  @JsonIgnore
  lazy val url = s"$protocol://$host:$port$path"
}

trait ServiceMappings {

  @Valid
  @NotNull
  @JsonProperty
  val serviceMappings: Map[String, ServiceConfiguration] = Map.empty
}

trait BaseConfiguration extends Configuration with EnvironmentConfiguration with ClientConfiguration {

  @Valid
  @NotNull
  @JsonProperty
  val mongodb = new MongoConf
}

