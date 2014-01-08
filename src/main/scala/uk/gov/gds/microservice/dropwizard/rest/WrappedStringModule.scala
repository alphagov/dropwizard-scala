package uk.gov.gds.microservice.dropwizard.rest

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ SerializerProvider, DeserializationContext }
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import uk.gov.gds.microservice.utils.Logging
import com.fasterxml.jackson.core.{ JsonGenerator, JsonToken, JsonParser }
import scala.reflect.ClassTag
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer

class WrappedStringModule[T](clazs: Class[T])(implicit t: ClassTag[T]) extends SimpleModule(clazs.getName + "Module") {
  addSerializer(new WrappedStringSerializer[T](clazs))
  addDeserializer(clazs, new WrappedStringDeserializer[T])

  private class WrappedStringDeserializer[T](implicit t: ClassTag[T]) extends StdScalarDeserializer[T](t.runtimeClass) with Logging {
    override def deserialize(jsonParser: JsonParser, context: DeserializationContext): T = {
      Option(jsonParser.getValueAsString) match {
        case Some(value: String) => instance(value)
        case None if jsonParser.getCurrentToken == JsonToken.VALUE_NULL => null.asInstanceOf[T]
        case _ => {
          debug("Failed to deserialize to " + t.runtimeClass.toString)
          throw context.mappingException("Expected String for " + t.runtimeClass)
        }
      }
    }

  }

  def instance[T](value: String)(implicit t: ClassTag[T]): T = {
    t.runtimeClass.asInstanceOf[Class[T]].getConstructor(classOf[String]).newInstance(value)
  }

  private class WrappedStringSerializer[T](cls: Class[T])(implicit t: ClassTag[T]) extends StdScalarSerializer[T](cls) {
    override def serialize(value: T, generator: JsonGenerator, serializerProvider: SerializerProvider) {
      generator.writeString(value.toString)
    }
  }

}

