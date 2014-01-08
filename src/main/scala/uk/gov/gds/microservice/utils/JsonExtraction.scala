package uk.gov.gds.microservice.utils

object JsonExtraction extends Logging {

  import java.net.URI
  import org.json4s._
  import org.json4s.jackson.JsonMethods._
  import org.json4s.JsonAST.JString

  val formats = DefaultFormats + json4sDateTimeSerializer + json4sLocalDateSerializer + UriSerializer + Json4sIntToBigDecimalSerializer

  def apply[A](body: String)(implicit m: Manifest[A], formats: Formats = formats): A = extractResponse[A](body)

  private def extractResponse[A](body: String)(implicit m: Manifest[A], format: Formats = formats): A = Option(body) match {
    case Some(b) if b.length > 0 => {
      try {
        parse(b, useBigDecimalForDouble = true).extract
      } catch {
        case t: Throwable => {
          debug(s"Failed to extract json into type ${m.runtimeClass.getName}")
          throw t
        }
      }
    }
    case _ => throw new IllegalArgumentException("A string value is required for transformation")
  }

  case object UriSerializer extends CustomSerializer[URI](format => ({
    case JString(uri) => URI.create(uri)
    case JNull => null
  }, {
    case uri: URI => JString(uri.toString)
  }
  ))

}