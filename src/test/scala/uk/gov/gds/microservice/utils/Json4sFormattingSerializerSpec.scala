package uk.gov.gds.microservice.utils

import uk.gov.gds.microservice.test.BaseSpec
import org.scalatest.matchers.MustMatchers
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar.{ mock => smock }
import org.scalatest.{ ParallelTestExecution, BeforeAndAfter }
import org.json4s.{ MappingException, DefaultFormats }
import org.json4s.JsonAST.{ JNull, JString }
import org.mockito.Mockito._
import org.json4s.reflect.TypeInfo

case class DummyObject(str: String)

class Json4sFormattingSerializerSpec extends BaseSpec with MustMatchers with BeforeAndAfter with ParallelTestExecution {

  private val mockFormatter = smock[Formatter[DummyObject]]

  private val serializer = new Json4sFormattingSerializer[DummyObject](mockFormatter)

  private implicit val formats = DefaultFormats + serializer

  "Json4sFormattingSerializer.serialize" should {

    "correctly call the formatter with the value to format if its type matches" in {
      val obj = DummyObject("abc")
      when(mockFormatter.format(obj)).thenReturn("def")
      serializer.serialize(formats)(obj) must equal(JString("def"))
    }
  }

  "Json4sFormattingSerializer.deserialize" should {

    val typeInfo = TypeInfo(classOf[DummyObject], None)

    "correctly call the formatter to parse the input and return our object" in {
      val obj = DummyObject("abc")
      when(mockFormatter.parse("blah")).thenReturn(Right(obj))
      serializer.deserialize(formats)((typeInfo, JString("blah"))) must equal(obj)
    }

    "return null if the input string is null" in {
      serializer.deserialize(formats)((typeInfo, JNull)) must equal(null)
      verifyNoMoreInteractions(mockFormatter)
    }

    "throw a MappingException if the input can't be parsed" in {
      when(mockFormatter.parse("blah")).thenReturn(Left("can't parse that"))

      val thrown = intercept[MappingException] {
        serializer.deserialize(formats)((typeInfo, JString("blah")))
      }

      thrown.getMessage.contains("can't parse that") must equal(true)
    }
  }
}
