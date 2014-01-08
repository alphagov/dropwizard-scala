package uk.gov.gds.microservice.auth

import com.google.common.base.Optional
import com.sun.jersey.api.client.{ ClientResponse, Client }
import javax.ws.rs.core.MediaType
import com.fasterxml.jackson.annotation.{ JsonCreator, JsonProperty }
import uk.gov.gds.microservice.utils.Logging

case class AuthorisationRequest @JsonCreator() (@JsonProperty authId: String,
  @JsonProperty resourceId: String,
  @JsonProperty regime: String)

case class AuthorisedUser(authId: String)

class Authoriser(client: Client, config: AuthorisationConfiguration) extends Logging {

  def authorise(credentials: AuthorisationRequest): Optional[AuthorisedUser] = {
    val resource = client.resource(config.url)

    try {
      val response = resource.`type`(MediaType.APPLICATION_JSON).post(classOf[ClientResponse], credentials)
      debug(response.toString)
      response.getStatus match {
        case 200 => Optional.of(AuthorisedUser(credentials.authId))
        case _ => Optional.absent()
      }
    } catch {
      case ex: Exception => {
        error("Authorisation check failed", ex)
        Optional.absent()
      }
    }
  }
}

