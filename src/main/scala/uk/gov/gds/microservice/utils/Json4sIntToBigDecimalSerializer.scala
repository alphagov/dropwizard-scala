package uk.gov.gds.microservice.utils

import org.json4s._
import scala.math.BigDecimal

/*
  TODO Remove this once JSON4S defect 44 is fixed:
  See: https://github.com/json4s/json4s/issues/44
  This is fixed, but not released, see: https://github.com/json4s/json4s/pull/45
*/

object Json4sIntToBigDecimalSerializer extends CustomSerializer[BigDecimal](
  format => (
    {
      case JInt(value: BigInt) => BigDecimal(value)
      case JNull => null.asInstanceOf[BigDecimal]
    },
    {
      case value: BigDecimal => JDecimal(value)
    }
  ))