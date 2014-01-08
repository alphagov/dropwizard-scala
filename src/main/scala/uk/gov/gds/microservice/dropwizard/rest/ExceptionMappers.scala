package uk.gov.gds.microservice.dropwizard.rest

import java.net.URI
import javax.ws.rs.WebApplicationException
import javax.ws.rs.ext.ExceptionMapper
import uk.gov.gds.microservice.utils.Logging

case class ErrorResponse(statusCode: Int, message: String, xStatusCode: Option[String] = None, requested: Option[URI] = None)

abstract class FailureResponse(statusCode: Int, message: String, xStatusCode: Option[String] = None, requestedResource: Option[URI] = None) extends GenericFailureResponse(statusCode, ErrorResponse(statusCode, message, xStatusCode, requestedResource))

abstract class GenericFailureResponse(statusCode: Int, body: Any) extends WebApplicationException with RestPredef {
  val response = builder(body, statusCode)
}

object FailureResponses {

  case class BadRequestException(message: String, xStatusCode: Option[String] = None) extends FailureResponse(400, message, xStatusCode)

  case class BadRequestFieldValidationException(fieldDefinition: Map[String,String]) extends GenericFailureResponse(400, fieldDefinition)

  case class UnauthorizedException(message: String, xStatusCode: Option[String] = None) extends FailureResponse(401, message, xStatusCode)

  case class ForbiddenException(message: String, xStatusCode: Option[String] = None) extends FailureResponse(403, message, xStatusCode)

  case class NotFoundException(message: String, requestedResource: Option[URI] = None, xStatusCode: Option[String] = None) extends FailureResponse(404, message, xStatusCode, requestedResource)

  case class GoneException(message: String, xStatusCode: Option[String] = None) extends FailureResponse(410, message, xStatusCode)

  case class PreconditionFailedException(message: String, xStatusCode: Option[String] = None) extends FailureResponse(412, message, xStatusCode)

  case class InternalServerException(message: String) extends FailureResponse(500, message)

}

class FailureResponseMapper extends ExceptionMapper[GenericFailureResponse] with Logging {
  def toResponse(failureResponse: GenericFailureResponse) = {
    debug(s"Failure response of : ${failureResponse.toString}")
    failureResponse.response
  }
}

class GenericErrorResponseMapper extends ExceptionMapper[Exception] with Logging with RestPredef {

  def toResponse(unexpectedError: Exception) = {
    error(s"Unexpected error : $unexpectedError", unexpectedError)
    builder(ErrorResponse(500, unexpectedError.getMessage), 500)
  }
}

case class SuccessResponse(statusCode: Int, message: Option[String], requested: Option[URI] = None)

abstract class SuccessfulResponse(statusCode: Int, val logMsg: Option[String] = None, message: Option[String] = None, requestedResource: Option[URI] = None) extends WebApplicationException with RestPredef {
  val response = builder(SuccessResponse(statusCode, message, requestedResource), statusCode)
}

object SuccessfulResponses {

  import Resource._

  case class NoContentResponse(msgToLog: String) extends SuccessfulResponse(204, Some(msgToLog)) {
    override val response = NoContent
  }

}

class SuccessResponseMapper extends ExceptionMapper[SuccessfulResponse] with Logging {
  def toResponse(successResponse: SuccessfulResponse) = {
    debug(successResponse.logMsg.getOrElse("No log message set for success response"))
    successResponse.response
  }
}
