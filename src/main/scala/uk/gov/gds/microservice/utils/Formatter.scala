package uk.gov.gds.microservice.utils

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ LocalDate, DateTime }

abstract class Formatter[T](implicit t: Manifest[T]) {
  def parse(str: String): Either[String, T]
  def format(value: T): String
  val formatString: String

  protected def parseError(str: String) = Left(s"Unable to parse '$str' to type '${t.runtimeClass.getSimpleName}', expected a valid value with format: $formatString")
}

object dateTimeFormatter extends Formatter[DateTime] {

  private val dateTimeFormatterOut = ISODateTimeFormat.dateTime.withZoneUTC
  private val dateTimeFormatterIn = ISODateTimeFormat.dateTime

  override def parse(str: String): Either[String, DateTime] = try {
    Right(dateTimeFormatterIn.parseDateTime(str))
  } catch {
    case e: IllegalArgumentException => parseError(str)
  }

  override def format(value: DateTime): String = {
    dateTimeFormatterOut.print(value)
  }

  override val formatString = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"
}

object localDateFormatter extends Formatter[LocalDate] {

  private val localDateRegex = """^(\d\d\d\d)-(\d\d)-(\d\d)$""".r

  override def parse(str: String): Either[String, LocalDate] = str match {
    case localDateRegex(y, m, d) => {
      try {
        Right(new LocalDate(y.toInt, m.toInt, d.toInt))
      } catch {
        case e: IllegalArgumentException => parseError(str)
      }
    }
    case _ => parseError(str)
  }

  override def format(value: LocalDate): String = "%04d-%02d-%02d".format(value.getYear, value.getMonthOfYear, value.getDayOfMonth)

  override val formatString = "yyyy-MM-dd"
}
