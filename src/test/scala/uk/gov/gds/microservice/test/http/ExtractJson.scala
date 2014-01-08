package uk.gov.gds.microservice.test.http

import javax.ws.rs.core.Response.Status
import uk.gov.gds.microservice.utils.{ JsonExtraction, Logging }
import scala.Some
import scala.io.Source

trait ExtractJson extends Logging {

  import TestRestClient._

  implicit val formats = JsonExtraction.formats

  protected lazy val httpGET: HTTPGet = client
  protected lazy val httpPOST: HTTPPost = client
  protected lazy val httpPUT: HTTPPut = client
  protected lazy val httpDELETE: HTTPDelete = client

  protected implicit def status2statuscode(sc: Status) = sc.getStatusCode

  protected case class ServiceResponse[A](serialized: A, clientResponse: ClientResponse)

  protected def extractJson[A](f: => Either[Throwable, ClientResponse], expectedStatusCode: Int = Status.OK)(implicit m: Manifest[A]): ServiceResponse[A] =
    f match {
      case Right(response) if response.statusCode == expectedStatusCode => {
        debug(s"Response body : ${response.responseBody}")
        ServiceResponse(JsonExtraction[A](response.responseBody), response)
      }
      case Right(response) =>
        throw new Exception(s"Response status code was ${response.statusCode} rather than $expectedStatusCode")
      case Left(failure) =>
        throw new Exception("Exception during http request", failure)
    }

  private object TestRestClient {
    lazy val client = new PooledHttpRestClient(10, Some(1200000))
  }

  protected def jsonFromFile(file: String): String = Source.fromURL(getClass.getClassLoader.getResource(file)).mkString
}