package uk.gov.gds.microservice.dropwizard.rest

import uk.gov.gds.microservice.utils.Logging
import scala.util.{ Failure, Success, Try }

trait RestPredef {

  import javax.ws.rs.core.{ MediaType, Response }
  import Response._
  import javax.ws.rs.core.Response.ResponseBuilder

  final val ApplicationJson = MediaType.APPLICATION_JSON + ";charset=utf-8"

  implicit def build(builder: ResponseBuilder): Response = builder.`type`(ApplicationJson).build()

  implicit def intToStatusCode(code: Int) = Status.fromStatusCode(code)

  def builder(entity: Any, responseStatus: Status): Response = status(responseStatus).entity(entity)
}

object Resource extends Logging with RestPredef {

  import uk.gov.gds.microservice.dropwizard.rest.SuccessfulResponses.NoContentResponse
  import uk.gov.gds.microservice.dropwizard.rest.FailureResponses.NotFoundException
  import javax.ws.rs.core.Response
  import Response._
  import java.net.URI

  val Ok: Response = ok()

  def Ok(entity: Any): Response = ok(entity)

  def Created(location: URI): Response = created(location)

  val NoContent: Response = noContent()

  def OkOrNotFound(entity: Option[Any], notFoundMsg: => String): Response = Ok(getOrNotFound[Any](entity, notFoundMsg))

  def getOrNotFound[A](entity: Option[A], notFoundMsg: => String): A = getOrType[A, NotFoundException](entity, NotFoundException(notFoundMsg))

  def getOrNoContent[A](entity: Option[A], logMsg: => String = "Failed to find the resource"): A = getOrType[A, NoContentResponse](entity, NoContentResponse(logMsg))

  def getOrType[A, F <: Throwable](entity: Option[A], failure: F): A = entity match {
    case Some(e) => e
    case _ => throw failure
  }

  def transform[FROM, TO](result: Try[FROM], f: FROM => TO): TO = {

    result match {
      case Success(data) => f(data)
      case Failure(error) => throw NotFoundException(error.getMessage)
    }
  }
}
