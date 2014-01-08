package uk.gov.gds.microservice.utils

import org.json4s._
import org.joda.time.{ LocalDate, DateTime }
import org.json4s.JsonAST.JString

class Json4sFormattingSerializer[T](formatter: Formatter[T])(implicit manifest: Manifest[T]) extends CustomSerializer[T](format => (
  {
    case JString(str) => {
      formatter.parse(str) match {
        case Right(value: T) => value
        case Left(error) => throw new MappingException(error)
      }
    }
    case JNull => null.asInstanceOf[T]
  },
  {
    case value: T => JString(formatter.format(value))
  }
))

case object json4sDateTimeSerializer extends Json4sFormattingSerializer[DateTime](dateTimeFormatter)
case object json4sLocalDateSerializer extends Json4sFormattingSerializer[LocalDate](localDateFormatter)
