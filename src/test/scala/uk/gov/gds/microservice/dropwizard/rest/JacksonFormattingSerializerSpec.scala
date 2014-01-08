package uk.gov.gds.microservice.dropwizard.rest

import uk.gov.gds.microservice.test.BaseSpec
import org.scalatest.matchers.MustMatchers
import org.mockito.Mockito.{ reset, when, verify, verifyNoMoreInteractions }
import com.fasterxml.jackson.core.JsonGenerator
import org.scalatest.mock.MockitoSugar.{ mock => smock }
import com.fasterxml.jackson.databind.SerializerProvider
import org.scalatest.{ ParallelTestExecution, BeforeAndAfter }
import uk.gov.gds.microservice.utils.Formatter

class JacksonFormattingSerializerSpec extends BaseSpec with MustMatchers with BeforeAndAfter with ParallelTestExecution {

  private val mockSerializerProvider = smock[SerializerProvider]
  private val mockJsonGenerator = smock[JsonGenerator]
  private val mockFormatter = smock[Formatter[Long]]

  private var serializer: JacksonFormattingSerializer[Long] = null

  before {
    reset(mockSerializerProvider, mockJsonGenerator, mockFormatter)
    serializer = new JacksonFormattingSerializer[Long](mockFormatter)
  }

  "Calling JacksonFormattingSerializer.serialize" should {
    "call the formatter to format the value and write it to the generator" in {
      when(mockFormatter.format(1234L)).thenReturn("one two three four")

      serializer.serialize(1234L, mockJsonGenerator, mockSerializerProvider)

      verify(mockFormatter).format(1234L)
      verify(mockJsonGenerator).writeString("one two three four")
      verifyNoMoreInteractions(mockFormatter, mockJsonGenerator, mockSerializerProvider)
    }
  }
}

