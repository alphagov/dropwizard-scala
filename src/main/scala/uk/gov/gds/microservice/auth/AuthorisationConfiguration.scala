package uk.gov.gds.microservice.auth

import javax.validation.constraints.NotNull
import com.fasterxml.jackson.annotation.{ JsonIgnore, JsonProperty }

trait AuthorisationConfiguration {

  @NotNull
  @JsonProperty
  val protocol: String = "NOT CONFIGURED"

  @NotNull
  @JsonProperty
  val host: String = "NOT CONFIGURED"

  @NotNull
  @JsonProperty
  val port: Int = -1

  @NotNull
  @JsonProperty
  val path: String = "NOT CONFIGURED"

  @JsonIgnore
  lazy val url = s"$protocol://$host:$port$path"
}
