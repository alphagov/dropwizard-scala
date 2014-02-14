package uk.gov.gds.microservice.dropwizard.rest

import org.mockito.{Mockito => mock}
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar.{mock => smock}
import org.scalatest.GivenWhenThen

class BuildInfoHealthCheckSpec extends WordSpec with MustMatchers with GivenWhenThen {

  "check" should {
    "display the package implementation version" in {
      Given("a manifest with an implementation version declared")
      val p = smock[Package]
      mock.when(p.getImplementationVersion()).thenReturn("IMPLEMENTATION-VERSION")

      When("the build info health is checked")
      val check = new BuildInfoHealthCheck(p).check()

      Then("the message should display the implementation version as the build number")
      assert(check.getMessage().endsWith("IMPLEMENTATION-VERSION"))
    }

    "notify on an unavailable build number" in {
      Given("a manifest without an implementation version declared")
      val p = smock[Package]

      When("the build info health is checked")
      val check = new BuildInfoHealthCheck(p).check()

      Then("the message should display '[not available]' as the builder number")
      assert(check.getMessage().endsWith("[not available]"))
    }
  }

}
