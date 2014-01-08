package uk.gov.gds.microservice.dropwizard.rest

import uk.gov.gds.microservice.test.BaseSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.{ ParallelTestExecution, BeforeAndAfter }
import org.scalatest.mock.MockitoSugar.{ mock => smock }
import uk.gov.gds.microservice.utils.Formatter
import com.fasterxml.jackson.core.{ JsonToken, JsonParseException, JsonParser }
import com.fasterxml.jackson.databind.DeserializationContext
import org.mockito.Mockito._

class JacksonFormattingDeserializerSpec extends BaseSpec with MustMatchers with BeforeAndAfter with ParallelTestExecution {

  private val mockFormatter = smock[Formatter[Long]]
  private val mockParser = smock[JsonParser]
  private val mockContext = smock[DeserializationContext]

  private var deserializer: JacksonFormattingDeserializer[Long] = null

  before {
    reset(mockContext, mockParser, mockFormatter)
    deserializer = new JacksonFormattingDeserializer[Long](mockFormatter)
  }

  "Calling JacksonFormattingSerializer.deserialize" should {

    "get the next token string, parse and return it" in {

      when(mockParser.getValueAsString).thenReturn("one two three four")
      when(mockFormatter.parse("one two three four")).thenReturn(Right(1234L))

      deserializer.deserialize(mockParser, mockContext) must equal(1234L)

      verify(mockParser).getValueAsString
      verify(mockFormatter).parse("one two three four")
      verifyNoMoreInteractions(mockFormatter, mockParser, mockContext)
    }

    "get the next token string, parse it, and throw an exception if there's a parse error" in {

      when(mockParser.getValueAsString).thenReturn("one two three four")
      when(mockFormatter.parse("one two three four")).thenReturn(Left("can't parse that"))

      intercept[JsonParseException] {
        deserializer.deserialize(mockParser, mockContext)
      }

      verify(mockFormatter).parse("one two three four")
      verify(mockParser).getValueAsString
      verify(mockParser).getCurrentLocation
      verifyNoMoreInteractions(mockFormatter, mockParser, mockContext)
    }

    "get the next token string, if it's null, confirm that token type is correct, and return null" in {

      when(mockParser.getValueAsString).thenReturn(null)
      when(mockParser.getCurrentToken).thenReturn(JsonToken.VALUE_NULL)

      deserializer.deserialize(mockParser, mockContext)

      verify(mockParser).getValueAsString
      verify(mockParser).getCurrentToken
      verifyNoMoreInteractions(mockFormatter, mockParser, mockContext)
    }

    "get the next token string, if it's null and the token type is not VALUE_NULL, then throw a parse exception" in {

      when(mockParser.getValueAsString).thenReturn(null)
      when(mockParser.getCurrentToken).thenReturn(JsonToken.VALUE_EMBEDDED_OBJECT)

      intercept[JsonParseException] {
        deserializer.deserialize(mockParser, mockContext)
      }

      verify(mockParser).getValueAsString
      verify(mockParser, times(2)).getCurrentToken
      verify(mockParser).getCurrentLocation
      verifyNoMoreInteractions(mockFormatter, mockParser, mockContext)
    }
  }
}
