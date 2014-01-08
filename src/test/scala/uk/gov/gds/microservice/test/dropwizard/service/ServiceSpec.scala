package uk.gov.gds.microservice.test.dropwizard.service

import org.scalatest.BeforeAndAfterAll
import uk.gov.gds.microservice.utils.Logging
import uk.gov.gds.microservice.test.http.{ ExtractJson, ClientResponse }
import uk.gov.gds.microservice.dropwizard.rest.ErrorResponse
import uk.gov.gds.microservice.test.BaseSpec

trait ServiceSpec[C <: uk.gov.gds.microservice.dropwizard.conf.BaseConfiguration, S <: uk.gov.gds.microservice.dropwizard.conf.MicroService[C]] extends MicroServiceEmbeddedServer[C, S] with BaseSpec with BeforeAndAfterAll with ExtractJson with Logging {

  override protected def beforeAll() {
    start(fail("Service ended immediately after starting."))
  }

  override protected def afterAll() {
    stop()
  }

  protected def extractErrorResponse(f: => Either[Throwable, ClientResponse], expectedStatusCode: Int, errorMessage: Option[String] = None): ServiceResponse[ErrorResponse] = {
    val sr = extractJson[ErrorResponse](f, expectedStatusCode)
    val serialized = sr.serialized
    serialized.statusCode mustBe expectedStatusCode

    if (errorMessage.isDefined) serialized.message mustBe errorMessage.get

    sr
  }

  protected def noContentResponse(f: => Either[Throwable, ClientResponse]): ClientResponse =
    f match {
      case Right(cr) if cr.statusCode == 204 => cr.responseBody must be('empty); cr
      case cr @ _ => fail(s"Failed with response of : $cr")
    }

  protected def verifyStatusCodeOnly(f: => Either[Throwable, ClientResponse], expectedStatusCode: Int = 200): ClientResponse =
    f match {
      case Right(cr) if cr.statusCode == expectedStatusCode => assert(true); cr
      case cr @ _ => fail(s"Failed with response of : $cr")
    }
}