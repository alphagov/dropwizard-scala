package uk.gov.gds.microservice.rest.client

import uk.gov.gds.microservice.test.BaseSpec
import uk.gov.gds.microservice.dropwizard.rest.client.ServiceClient
import com.sun.jersey.api.client.Client
import org.scalatest.matchers.ShouldMatchers
import uk.gov.gds.microservice.dropwizard.conf.ServiceKey

class ExternalResourceSpec extends BaseSpec with ShouldMatchers {

  import org.scalatest.PartialFunctionValues._

  val client = new Client()

  "ExternalResource companion object" should {
    "be created from a map" in {

      val actual = ServiceClient(Some(Map("auth" -> "http://auth", "personal" -> "http://personal", "nps" -> "http://nps")), client)

      actual must not equal None

      (actual.get valueAt ServiceKey("auth")).isInstanceOf[ServiceClient] === true
      (actual.get valueAt ServiceKey("personal")).isInstanceOf[ServiceClient] === true
      (actual.get valueAt ServiceKey("nps")).isInstanceOf[ServiceClient] === true
    }

    "return None" in {
      ServiceClient(None, client) must be(None)
    }
  }
}
