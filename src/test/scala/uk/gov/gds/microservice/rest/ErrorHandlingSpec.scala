package uk.gov.gds.microservice.rest

import javax.ws.rs._
import scala.Array
import uk.gov.gds.microservice.dropwizard.rest.Resource
import javax.ws.rs.core.Response
import java.net.URI
import com.yammer.metrics.core.HealthCheck
import com.yammer.metrics.core.HealthCheck.Result
import uk.gov.gds.microservice.test.http.ClientResponse
import uk.gov.gds.microservice.test.dropwizard.service.MicroServiceEmbeddedServer

@Path("/errorhandling")
@Consumes(Array(Resource.ApplicationJson))
@Produces(Array(Resource.ApplicationJson))
class ErrorHandlingTestResource {

  import Resource._
  import uk.gov.gds.microservice.dropwizard.rest.SuccessfulResponses.NoContentResponse
  import uk.gov.gds.microservice.dropwizard.rest.FailureResponses._

  @GET
  @Path("/error/{code}")
  def throwAppropriateResponse(@PathParam("code") code: Int): Response = {
    code match {
      case 200 => Ok("Ok")
      case 204 => throw new NoContentResponse("A message from logging not sent back in the response")
      case 400 => throw new BadRequestException("Bad request", Some("12982-is-a-meaningful-xStatusCode"))
      case 401 => throw new UnauthorizedException("Unauthorized", Some("a-more-specific-reason-as-to-why-they-are-unauthorised"))
      case 403 => throw new ForbiddenException("Forbidden", Some("a-more-specific-reason-as-to-why-they-are-forbidden"))
      case 404 => throw new NotFoundException("Not found", Some(URI.create(s"/errorhandling/error/$code")), Some("12982-is-a-meaningful-xStatusCode"))
      case 410 => throw new GoneException("Gone", Some("why-where-it-has-gone-xstatuscode"))
      case 500 => throw new InternalServerException("Unexpected condition has occurred and only option is to throw an error")
    }
  }

  @GET
  @Path("/error/util/{code}")
  def useResourceObjectUtilFunctions(@PathParam("code") code: Int): Response = {
    code match {
      case 200 => OkOrNotFound(Some("OK"), "No associated resource...")
      case 404 => OkOrNotFound(None, "No associated resource...")
    }
  }

  @GET
  @Path("/error/unexpected")
  def unexpectedFlowInTheCode: Response = {
    throw new Exception("An unexpected error occurred")
  }
}

class ErrorHandlingTestHealthCheck extends HealthCheck("ErrorHandlingTestResource") {

  def check() = Result.healthy()
}

class ErrorHandlingSpec extends TestAppMicroServiceSpec {

  def errorHandling(code: Int) = s"errorhandling/error/$code"

  val resources: List[Any] = List(new ErrorHandlingTestResource)
  override val healthChecks: Option[List[HealthCheck]] = Some(List(new ErrorHandlingTestHealthCheck))

  def request(expectedStatusCode: Int, msg: String, xStatusCode: Option[String] = None, requested: Option[URI] = None)(url: String = errorHandling(expectedStatusCode)): ClientResponse = {

    val serviceResponse = extractErrorResponse(httpGET.GET(resource(url)), expectedStatusCode)

    val errorResponse = serviceResponse.serialized
    msg mustBe (errorResponse.message)
    if (requested.isDefined) requested.get mustBe (errorResponse.requested.get)
    if (xStatusCode.isDefined) xStatusCode.get mustBe (errorResponse.xStatusCode.get)

    serviceResponse.clientResponse
  }

  "Successful response mapper" should {
    "200 all OK" in {
      httpGET.GET(resource(errorHandling(200))) match {
        case Right(cr) if (cr.statusCode == 200) => assert(true)
        case cr @ _ => fail(s"Failed with response of : $cr")
      }
    }
    "204 for no content" in {
      noContentResponse(httpGET.GET(resource(errorHandling(204))))
    }
  }

  "Failure response mapper" should {
    "400 Bad Request" in {
      request(400, "Bad request", Some("12982-is-a-meaningful-xStatusCode"))()
    }
    "401 Unauthorised" in {
      request(401, "Unauthorized", Some("a-more-specific-reason-as-to-why-they-are-unauthorised"))()
    }
    "403 Forbidden" in {
      request(403, "Forbidden", Some("a-more-specific-reason-as-to-why-they-are-forbidden"))()
    }
    "404 Not Found" in {
      request(404, "Not found", Some("12982-is-a-meaningful-xStatusCode"), Some(URI.create(s"/errorhandling/error/404")))()
    }

    "410 Gone" in {
      request(410, "Gone", Some("why-where-it-has-gone-xstatuscode"))()
    }
    "500 Internal Server explicitly thrown" in {
      request(500, "Unexpected condition has occurred and only option is to throw an error")()
    }
  }

  "Responses using utility Resource functions" should {

    "return 200 when entity found using OkOrNotFound" in {
      verifyStatusCodeOnly(httpGET.GET(resource("errorhandling/error/util/200")))
    }

    "return 404 when not found using OkOrNotFound" in {
      request(404, "No associated resource...")("errorhandling/error/util/404")
    }

  }

  "Generic error response mapper" should {
    "500 Internal Server thrown for unknown reason" in {
      request(500, "An unexpected error occurred")("errorhandling/error/unexpected")
    }
  }

}
