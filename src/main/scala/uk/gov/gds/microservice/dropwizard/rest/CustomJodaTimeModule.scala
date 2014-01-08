package uk.gov.gds.microservice.dropwizard.rest

import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind.{ DeserializationContext, SerializerProvider }
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import org.joda.time.{ LocalDate, DateTime }
import uk.gov.gds.microservice.utils.{ localDateFormatter, dateTimeFormatter, Formatter }
import com.fasterxml.jackson.databind.module.SimpleModule
import scala.Some

class CustomJodaTimeModule extends SimpleModule("CustomJodaTimeModule") {
  addSerializer(jacksonDateTimeSerializer)
  addDeserializer(classOf[DateTime], jacksonDateTimeDeserializer)
}

class JacksonFormattingSerializer[T](formatter: Formatter[T])(implicit t: Manifest[T]) extends StdScalarSerializer[T](t.runtimeClass.asInstanceOf[Class[T]]) {

  override def serialize(value: T, generator: JsonGenerator, serializerProvider: SerializerProvider) {
    generator.writeString(formatter.format(value))
  }
}

class JacksonFormattingDeserializer[T](formatter: Formatter[T])(implicit t: Manifest[T]) extends StdScalarDeserializer[T](t.runtimeClass.asInstanceOf[Class[T]]) {

  override def deserialize(jsonParser: JsonParser, context: DeserializationContext): T = {
    Option(jsonParser.getValueAsString) match {
      case Some(str: String) => formatter.parse(str) match {
        case Right(value) => value
        case Left(error) => throw new JsonParseException(error, jsonParser.getCurrentLocation)
      }
      case None if jsonParser.getCurrentToken == JsonToken.VALUE_NULL => null.asInstanceOf[T]
      case _ => throw new JsonParseException(s"Expected a string token representing type '${t.runtimeClass.getSimpleName}', found: ${jsonParser.getCurrentToken}", jsonParser.getCurrentLocation)
    }
  }
}

object jacksonDateTimeSerializer extends JacksonFormattingSerializer[DateTime](dateTimeFormatter)
object jacksonDateTimeDeserializer extends JacksonFormattingDeserializer[DateTime](dateTimeFormatter)

object jacksonLocalDateSerializer extends JacksonFormattingSerializer[LocalDate](localDateFormatter)
object jacksonLocalDateDeserializer extends JacksonFormattingDeserializer[LocalDate](localDateFormatter)

