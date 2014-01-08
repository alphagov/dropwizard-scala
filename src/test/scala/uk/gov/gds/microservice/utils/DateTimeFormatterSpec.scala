package uk.gov.gds.microservice.utils

import uk.gov.gds.microservice.test.BaseSpec
import org.scalatest.matchers.MustMatchers
import org.joda.time.{ DateTimeZone, DateTime }

class DateTimeFormatterSpec extends BaseSpec with MustMatchers {

  "dateTimeFormatter.format" should {

    "Format a DateTime object to the correct format in the UTC Timezone for a time in British Summer Time" in {
      val value = new DateTime(2013, 6, 4, 14, 47, 50, 323, DateTimeZone.forID("Europe/London")) // (GMT = UTC+0100)
      val expected = "2013-06-04T13:47:50.323Z"

      dateTimeFormatter.format(value) must equal(expected)
    }

    "Format a DateTime object to the correct format in the UTC timezone (for a time in winter)" in {
      val value = new DateTime(2013, 2, 22, 11, 44, 55, 544, DateTimeZone.forID("Europe/London")) // (GMT = UTC)
      val expected = "2013-02-22T11:44:55.544Z"

      dateTimeFormatter.format(value) must equal(expected)
    }

    "Format a DateTime object defined with some other timezone to the correct format in the UTC timezone" in {
      val value = new DateTime(2013, 3, 22, 10, 22, 44, 123, DateTimeZone.forID("EST")) // (Eastern Standard Time = UTC-0500)
      val expected = "2013-03-22T15:22:44.123Z" // 22 Mar 2013 15:22:44.123 UTC

      dateTimeFormatter.format(value) must equal(expected)
    }
  }

  "dateTimeFormatter.parse" should {

    "Parse a DateTime object which is in the correct UTC format for a time (in June)" in {
      val str = "2013-06-04T13:47:50.323Z"
      val expected = new DateTime(1370353670323L)

      dateTimeFormatter.parse(str) must equal(Right(expected))
    }

    "Parse a DateTime object which is in the correct UTC format for a time (in February)" in {
      val str = "2013-02-22T11:44:55.544Z"
      val expected = new DateTime(1361533495544L)

      dateTimeFormatter.parse(str) must equal(Right(expected))
    }

    "Parse a DateTime object which is in some other timezone (in June)" in {
      val str = "2013-06-05T04:46:22.434+10:00"
      val expected = new DateTime(1370371582434L)

      dateTimeFormatter.parse(str) must equal(Right(expected))
    }

    "Parse a DateTime object which is in some other timezone (in February)" in {
      val str = "2013-02-21T20:44:55.544-08:00"
      val expected = new DateTime(1361508295544L)

      dateTimeFormatter.parse(str) must equal(Right(expected))
    }

    "Return an error message if the date is invalid" in {
      dateTimeFormatter.parse("blah blah") must equal(Left("Unable to parse 'blah blah' to type 'DateTime', expected a valid value with format: yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
    }

    "Return an error message if the date is empty" in {
      dateTimeFormatter.parse("") must equal(Left("Unable to parse '' to type 'DateTime', expected a valid value with format: yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
    }

    "Return an error message if the date parses but is invalid" in {
      val str = "2013-02-29T20:44:55.544-08:00"
      dateTimeFormatter.parse(str) must equal(Left("Unable to parse '2013-02-29T20:44:55.544-08:00' to type 'DateTime', expected a valid value with format: yyyy-MM-dd'T'HH:mm:ss.SSSZZ"))
    }
  }
}
