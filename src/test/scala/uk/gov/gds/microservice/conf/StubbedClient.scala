package uk.gov.gds.microservice.conf

import uk.gov.gds.microservice.dropwizard.rest.client.ServiceClient
import uk.gov.gds.microservice.test.http.ExtractJson
import com.sun.jersey.api.client.ClientResponse
import org.mockito.Mockito
import scala.util.Try
import org.json4s.Formats
import uk.gov.gds.microservice.utils.JsonExtraction

abstract class StubbedClient(implicit formats: Formats = JsonExtraction.formats) extends ServiceClient(null, "http://somewhere") with ExtractJson {

  val testResponses: Map[String, ClientResponse]

  def toClientResponse(sc: Int, json: String = "") = {
    val r = Mockito.mock(classOf[ClientResponse])
    Mockito.when(r.getStatus).thenReturn(sc)
    if (sc == 200) Mockito.when(r.getEntity(classOf[String])).thenReturn(json)
    r
  }

  override def httpGet[T](path: String)(implicit m: Manifest[T]): Try[T] = {
    val r = testResponses.get(path).get
    processStatus(r)(extractJSONResponse[T])
  }

  override def doPost[T, R](path: String, body: Option[Any])(handleResponse: (ClientResponse) => T)(implicit m: Manifest[T]): Try[T] = {
    debug(s"POST to path : $path")
    processStatus(testResponses.get(path).get)(handleResponse)
  }

}