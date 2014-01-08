package uk.gov.gds.microservice.test.http

import org.apache.http.client.methods._
import java.io._
import org.apache.http.entity.AbstractHttpEntity
import java.net.URLEncoder
import scala.Some
import scala.Tuple2
import uk.gov.gds.microservice.utils.Logging
import org.json4s.Formats
import org.json4s.DefaultFormats

trait HTTPPost {
  self: PooledHttpRestClient =>

  def POST(uri: String, body: Option[Any], compress: Boolean = true): Either[Throwable, ClientResponse] = {
    this.POST(uri, body, self.applicationJsonHeaders, compress)
  }

  def POST(uri: String, body: Option[Any], headers: Option[Seq[Tuple2[String, String]]], compress: Boolean): Either[Throwable, ClientResponse] = {
    self.execute(new HttpPost(uri), body, headers, compress)
  }
}

trait HTTPPut {
  self: PooledHttpRestClient =>

  def PUT(uri: String, body: Option[Any], compress: Boolean): Either[Throwable, ClientResponse] = {
    PUT(uri, body, self.applicationJsonHeaders, compress)
  }

  def PUT(uri: String, body: Option[Any], headers: Option[Seq[Tuple2[String, String]]], compress: Boolean): Either[Throwable, ClientResponse] = {
    self.execute(new HttpPut(uri), body, headers, compress)
  }
}

trait HTTPGet {
  self: PooledHttpRestClient =>

  private def GET(httpGet: HttpGet): Either[Throwable, ClientResponse] = {
    self.execute(httpGet, None, None, false)
  }

  def GET(uri: String): Either[Throwable, ClientResponse] = {
    GET(new HttpGet(uri))
  }

  def GET(uri: String, queryParams: List[Tuple2[String, String]]): Either[Throwable, ClientResponse] = {
    GET(appendParams(uri, queryParams))
  }

  def urlEncode(in: String) = URLEncoder.encode(in, "UTF-8")

  def paramsToUrlParams(params: List[Tuple2[String, String]]): String = params.map {
    m => urlEncode(m._1) + "=" + urlEncode(m._2)
  }.mkString("&")

  def appendParams(url: String, params: List[Tuple2[String, String]]): String = params match {
    case xs if !url.contains("?") => url + "?" + paramsToUrlParams(xs)
    case xs => url + "&" + paramsToUrlParams(xs)
  }
}

trait HTTPDelete {
  self: PooledHttpRestClient =>

  private def DELETE(httpDelete: HttpDelete): Either[Throwable, ClientResponse] = {
    self.execute(httpDelete, None, None, false)
  }

  def DELETE(uri: String): Either[Throwable, ClientResponse] = {
    DELETE(new HttpDelete(uri))
  }
}

trait RestClient {
  val poolMaxTotal: Int
  val pooledHttpRestClient = new PooledHttpRestClient(poolMaxTotal)
}

class PooledHttpRestClient(poolMaxTotal: Int, requestTimeout: Option[Int] = Some(600000))(implicit formats: Formats = DefaultFormats) extends Logging with HTTPPost with HTTPPut with HTTPGet with HTTPDelete {

  import org.apache.http.impl.conn.PoolingClientConnectionManager
  import org.apache.http.impl.client.DefaultHttpClient
  import org.apache.http.Header
  import org.apache.http.client.protocol.{ ResponseContentEncoding, RequestAcceptEncoding }
  import org.apache.http.client.methods._
  import org.apache.http.{ HttpResponse, HttpEntity }
  import scala.Left
  import scala.Right
  import scala.collection.JavaConversions._
  import java.net.SocketTimeoutException
  import org.apache.http.util.EntityUtils
  import java.lang.{ Integer => JInteger }

  val applicationJsonHeaders = Some(Seq(("Content-Type" -> "application/json"), ("Accepts" -> "application/json")))

  val cm = new PoolingClientConnectionManager()
  cm.setMaxTotal(poolMaxTotal)
  cm.setDefaultMaxPerRoute(poolMaxTotal)

  private val httpClient = new DefaultHttpClient(cm)
  httpClient.addRequestInterceptor(new RequestAcceptEncoding())
  httpClient.addResponseInterceptor(new ResponseContentEncoding())

  requestTimeout match {
    case Some(n) => {
      httpClient.getParams.setParameter("http.socket.timeout", new JInteger(n))
      debug("Created PooledHttpRestClient with request timeout (http.socket.timeout): %s milliseconds".format(n))
    }
    case None => debug("Created PooledHttpRestClient with no request timeout (http.socket.timeout)")
  }

  def execute(request: HttpUriRequest, body: Option[Any], headers: Option[Seq[Tuple2[String, String]]], compress: Boolean): Either[Throwable, ClientResponse] = {
    addJsonBody(request, body, compress)

    debug("Adding headers")
    headers match {
      case Some(h) => h.map(m => request.addHeader(m._1, m._2))
      case _ => applicationJsonHeaders.get.map(m => request.addHeader(m._1, m._2))
    }
    debug("Added headers")

    try {
      debug("Executing http request to (%s)".format(request.getURI))
      val response = httpClient.execute(request)
      debug("Completed executing http request")

      val responseHeaders: Seq[(String, String)] = response.headerIterator.map(h => {
        val hd = h.asInstanceOf[Header]
        (hd.getName, hd.getValue)
      }).toSeq

      Right(new ClientResponse(response.getStatusLine.getStatusCode, parse(response), responseHeaders))
    } catch {
      case e: SocketTimeoutException if requestTimeout != None => {
        warn("Timeout making request to external service (%s) - timeout set to %dms".format(request.getURI, requestTimeout.get))
        Left(e)
      }
      case t: Throwable => {
        error("Failed to complete request to external service (" + request.getURI + ")", t)
        Left(t)
      }
    }
  }

  def shutDown() {
    httpClient.getConnectionManager.shutdown()
  }

  private def addJsonBody(request: HttpUriRequest, body: Option[Any], compress: Boolean) {
    request match {
      case r: HttpEntityEnclosingRequestBase if (!body.isEmpty) => r.setEntity(new JSONEntity(body, compress))
      case _ => debug("Request body not present / Request not of correct type")
    }
  }

  private def parse(response: HttpResponse): String = {
    val entity: HttpEntity = response.getEntity
    if (entity != null) EntityUtils.toString(entity, "UTF-8") else ""
  }
}

case class ClientResponse(statusCode: Int, responseBody: String, headers: Seq[(String, String)])

sealed case class ByteBufferOutputStream(override val size: Int) extends ByteArrayOutputStream(size) {
  def buffer = buf
}

case class JSONEntity(body: Option[Any], deflate: Boolean)(implicit formats: Formats) extends AbstractHttpEntity with Logging {

  import java.io._
  import java.util.zip.GZIPOutputStream
  import org.json4s._
  import org.json4s.jackson.JsonMethods._
  import scala.Some

  val json = body match {
    case Some(stringValue: String) => stringValue
    case Some(b) => {
      compact(render(Extraction.decompose(b))).toString
    }
    case _ =>
  }

  setContentType("application/json")
  var content: Array[Byte] = null
  var length: Int = 0

  val bytes = json.toString.getBytes("UTF-8")
  if (deflate) {
    val baos = new ByteArrayOutputStream()
    val deflater: GZIPOutputStream = new GZIPOutputStream(baos)
    try {
      deflater.write(bytes)
      deflater.flush()
      deflater.close()
      baos.close()

      content = baos.toByteArray
      length = content.size
      debug("Deflated JSON object size: " + length)
      setContentEncoding("gzip")
    } catch {
      case e: IOException => {
        this.content = bytes
        length = bytes.length
      }
    }
  } else {
    content = bytes
    length = bytes.length
    debug("Created JSON object size: " + length)
  }

  def getContentLength: Long = length

  def getContent: InputStream = new ByteArrayInputStream(content, 0, length)

  def writeTo(os: OutputStream) {
    if (os == null) {
      throw new IllegalArgumentException("Output stream may not be null")
    }
    os.write(content)
    os.flush()
    Unit
  }

  def isRepeatable: Boolean = true

  def isStreaming: Boolean = false
}

