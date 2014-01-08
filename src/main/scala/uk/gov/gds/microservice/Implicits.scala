package uk.gov.gds.microservice

import java.net.{ URL, URI }

object Implicits {
  implicit def stringToURI(s: String): URI = URI.create(s)

  implicit def stringToURL(s: String): URL = new URL(s)
}
