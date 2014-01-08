package uk.gov.gds.microservice.test

import java.net.URI

object SpecImplicits {
  implicit def stringToURI(s: String): URI = URI.create(s)
}
