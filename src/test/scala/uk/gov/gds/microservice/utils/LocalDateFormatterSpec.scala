package uk.gov.gds.microservice.utils

import org.scalatest.matchers.MustMatchers
import org.joda.time.LocalDate
import uk.gov.gds.microservice.test.BaseSpec

class LocalDateFormatterSpec extends BaseSpec with MustMatchers {

  "localTimeFormatter.format" should {

    "Format a LocalTime object to the correct format (for a date in British Summer Time)" in {
      localDateFormatter.format(new LocalDate(2013, 6, 4)) must equal("2013-06-04")
    }

    "Format a LocalDate object to the correct format (for a time in winter)" in {
      localDateFormatter.format(new LocalDate(2013, 2, 22)) must equal("2013-02-22")
    }
  }

  "localDateFormatter.parse" should {

    "Parse a LocalDate object which is in the correct format for a time (in June)" in {
      localDateFormatter.parse("2013-06-04") must equal(Right(new LocalDate(2013, 6, 4)))
    }

    "Parse a DateTime object which is in the correct UTC format for a time (in February)" in {
      localDateFormatter.parse("2013-02-22") must equal(Right(new LocalDate(2013, 2, 22)))
    }

    "Return an error message if the date is invalid" in {
      localDateFormatter.parse("blah blah") must equal(Left("Unable to parse 'blah blah' to type 'LocalDate', expected a valid value with format: yyyy-MM-dd"))
    }

    "Return an error message if the date is empty" in {
      localDateFormatter.parse("") must equal(Left("Unable to parse '' to type 'LocalDate', expected a valid value with format: yyyy-MM-dd"))
    }

    "Return an error message if the date is parseable but not value" in {
      localDateFormatter.parse("2013-02-29") must equal(Left("Unable to parse '2013-02-29' to type 'LocalDate', expected a valid value with format: yyyy-MM-dd"))
    }
  }
}
