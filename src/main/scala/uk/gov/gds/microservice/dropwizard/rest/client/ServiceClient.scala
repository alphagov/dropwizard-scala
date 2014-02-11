package uk.gov.gds.microservice.dropwizard.rest.client

import com.sun.jersey.api.client.{ ClientResponse, Client }
import uk.gov.gds.microservice.dropwizard.conf.{ ServiceKey }
import uk.gov.gds.microservice.utils.{ JsonExtraction, Logging }
import org.json4s.Formats
import uk.gov.gds.microservice.dropwizard.rest.FailureResponses
import uk.gov.gds.microservice.dropwizard.rest.SuccessfulResponses.NoContentResponse

class ServiceClient(client: Client, rootResourceUrl: String)(implicit formats: Formats = JsonExtraction.formats) extends Logging {

  import uk.gov.gds.microservice.utils.JsonExtraction
  import scala.util.{ Failure, Success, Try }
  import javax.ws.rs.core.MediaType

  private val ApplicationJson = MediaType.APPLICATION_JSON + ";charset=utf-8"

  private def /-(uri: String) = if (uri.endsWith("/")) uri.dropRight(1) else uri

  private def -/(uri: String) = if (uri.startsWith("/")) uri.drop(1) else uri

  private val root = /-(rootResourceUrl)

  @inline private def resource(res: String, accept: String = ApplicationJson) = client.resource(s"$root/${-/(res)}").`type`(ApplicationJson).accept(accept)

  //TODO : add other httpGet variants, i.e adding headers
  def httpGet[T](path: String)(implicit m: Manifest[T]): Try[T] = doGet[T](path)(extractJSONResponse[T])
  def httpPost[T](path: String, bodyOption: Option[Any])(implicit m: Manifest[T]): Try[T] = doPost[T, Try[T]](path, bodyOption)(extractJSONResponse[T])
  def httpPostNoResponse(path: String, bodyOption: Option[Any]): Try[ClientResponse] = doPost[ClientResponse, Try[ClientResponse]](path, bodyOption)(extractNoResponse)
  def httpPut[T](path: String, bodyOption: Option[Any])(implicit m: Manifest[T]): Try[T] = doPut[T, Try[T]](path, bodyOption)(extractJSONResponse[T])
  def httpPutNoResponse(path: String, bodyOption: Option[Any]): Try[ClientResponse] = doPut[ClientResponse, Try[ClientResponse]](path, bodyOption)(extractNoResponse)

  def httpDelete(path: String): Try[ClientResponse] = doDelete[ClientResponse](path)(extractNoResponse)


  def doGet[T](path: String)(handleResponse: (ClientResponse) => T)(implicit m: Manifest[T]): Try[T] = {
    val response = resource(path).get(classOf[ClientResponse])
    processStatus(response)(handleResponse)
  }

  protected def doPost[T, R](path: String, body: Option[Any])(handleResponse: (ClientResponse) => T)(implicit m: Manifest[T]): Try[T] = {
    val response = body match {
      case Some(b) => resource(path).post(classOf[ClientResponse], b)
      case None => resource(path).post(classOf[ClientResponse])
    }
    processStatus(response)(handleResponse)
  }

  protected def doPut[T, R](path: String, body: Option[Any])(handleResponse: (ClientResponse) => T)(implicit m: Manifest[T]): Try[T] = {
    val response = body match {
      case Some(b) => resource(path).put(classOf[ClientResponse], b)
      case None => resource(path).put(classOf[ClientResponse])
    }
    processStatus(response)(handleResponse)
  }

  protected def doDelete[T](path: String)(handleResponse: (ClientResponse) => T)(implicit m: Manifest[T]): Try[T] = {
    val response = resource(path).delete(classOf[ClientResponse])
    processStatus(response)(handleResponse)
  }

  protected def extractJSONResponse[T](response: ClientResponse)(implicit m: Manifest[T]): T = {
    try {
      debug("Received successful response")
      JsonExtraction[T](response.getEntity(classOf[String]))
    } catch {
      case e: Throwable => {
        error("Malformed result", e)
        throw new Exception("Malformed result")
      }
    }
  }

  protected def extractNoResponse(response: ClientResponse): ClientResponse = {
    response
  }

  protected def processStatus[T](response: ClientResponse)(handleResponse: (ClientResponse) => T)(implicit m: Manifest[T]): Try[T] = {
    response.getStatus match {
      case 200 | 201 | 204  => Success(handleResponse(response))
      case 404 =>
        warn("Received response with error 404")
        Failure(new FailureResponses.NotFoundException("Not found"))
      case 403 =>
        warn("Received response with error 403")
        Failure(new FailureResponses.ForbiddenException("Forbidden"))
      case 400 =>
        warn("Received response with error 400")
        Failure(new FailureResponses.BadRequestException("Bad Request"))
      case 412 =>
        warn("Received response with error 412")
        Failure(new FailureResponses.PreconditionFailedException("Precondition Failed"))
      case 500 =>
        warn(s"Received response with error 500")
        Failure(new FailureResponses.InternalServerException("Internal Server Error"))
      case x =>
        warn(s"Received response with error $x")
        throw new Exception(s"Unexpected response status ($x)")
    }
  }
}

object ServiceClient {
  def apply(externalResources: Option[Map[String, String]], client: Client)(implicit formats: Formats = JsonExtraction.formats): Option[Map[ServiceKey, ServiceClient]] = externalResources match {
    case Some(er) => Some(er.map { m => ServiceKey(m._1) -> new ServiceClient(client, er(m._1)) })
    case _ => None
  }
}