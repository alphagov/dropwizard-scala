package uk.gov.gds.microservice.utils

import java.net.URI
import org.json4s.MappingException
import uk.gov.gds.microservice.test.BaseSpec
import org.scalatest.matchers.ShouldMatchers
import org.joda.time.{ LocalDate, DateTime }

class JsonExtractionSpec extends BaseSpec with ShouldMatchers {

  implicit def stringToURI(s: String) = URI.create(s)

  "Json" should {
    "be transformed into associated object" in {
      JsonExtraction[VariousFields]("""{"id":"123456789","number": 87,"strings" : ["one", "two","three"], "date":"2013-08-22", "timestamp":"2013-05-11T23:44:22.645Z", "decimal":2345.1234}""") must
        be(VariousFields("123456789", 87, List("one", "two", "three"), new LocalDate(2013, 8, 22), new DateTime(1368315862645L), BigDecimal("2345.1234")))
    }

    "be transformed into the object type even if the json has more fields than the object" in {
      JsonExtraction[HasURIField]("""{"id":"123456789","number": 87,"strings" : ["one", "two","three"], "date":"2013-08-22", "timestamp":"2013-05-11T23:44:22.645Z"}""") must be(HasURIField("123456789"))
    }

    "fail transformation when the json has less fields than the object type" in {
      evaluating {
        JsonExtraction[VariousFields]("""{"id":"/has/uri/123456789"}""")
      } should produce[MappingException]
    }

    "with URI are deserializer" in {
      JsonExtraction[HasURIField]("""{"id":"/has/uri/123456789"}""") must be(HasURIField("/has/uri/123456789"))
    }

    "deserialise a BigDecimal" in {
      JsonExtraction[HasBigDecimalField]("""{"decimal":123.456}""") must be(HasBigDecimalField(BigDecimal("123.456")))
    }

    "deserialise a BigDecimal with no decimal places" in {
      JsonExtraction[HasBigDecimalField]("""{"decimal":123}""") must be(HasBigDecimalField(BigDecimal("123")))
    }

    "fail transformation when null json value is provided" in {
      val caught = evaluating {
        JsonExtraction[VariousFields](null)
      } should produce[IllegalArgumentException]

      caught.getMessage should be("A string value is required for transformation")
    }

    "fail transformation when empty string value is provided" in {
      evaluating {
        JsonExtraction[VariousFields]("")
      } should produce[IllegalArgumentException]
    }
  }

}

sealed case class VariousFields(id: String, number: Int, strings: List[String], date: LocalDate, timestamp: DateTime, decimal: BigDecimal)

sealed case class HasURIField(id: URI)

sealed case class HasBigDecimalField(decimal: BigDecimal)