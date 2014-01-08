package uk.gov.gds.microservice.dropwizard.conf

import com.yammer.dropwizard.config.{ Bootstrap, Environment, Configuration }
import com.yammer.dropwizard.{ Bundle, Service }
import javax.ws.rs.ext.ExceptionMapper
import uk.gov.gds.microservice.dropwizard.rest.{ CustomJodaTimeModule, SuccessResponseMapper }
import uk.gov.gds.microservice.utils.{ JsonExtraction, Logging }
import com.yammer.dropwizard.assets.AssetsBundle
import com.sun.jersey.api.client.Client
import com.yammer.dropwizard.client.{ JerseyClientConfiguration, JerseyClientBuilder }
import uk.gov.gds.microservice.dropwizard.rest.client.ServiceClient
import org.apache.http.conn.scheme.{ PlainSocketFactory, Scheme, SchemeRegistry }
import javax.net.ssl.{ X509TrustManager, SSLContext }
import scala.Array
import org.apache.http.conn.ssl.{ TrustStrategy, X509HostnameVerifier, SSLSocketFactory }
import java.security.cert.X509Certificate

trait CustomExceptionMappers {

  import scala.collection.mutable.ListBuffer
  import scala.collection.JavaConversions._

  def addMappers(environment: Environment, mappers: List[ExceptionMapper[_]], removeStandardExceptionMappers: Boolean = true) {
    if (removeStandardExceptionMappers) removeDropWizardExceptionMappers(environment)

    mappers.foreach(environment.addProvider(_))
  }

  /**
   * Seems DW has an issue with making there ExceptionMappers configurable. This solution makes me slightly nervous
   * but for now it is ok. See http://thoughtspark.org/2013/02/25/dropwizard-and-jersey-exceptionmappers/
   */
  def removeDropWizardExceptionMappers(environment: Environment) {
    val jrConfig = environment.getJerseyResourceConfig
    val dwSingletonsToRemove = jrConfig.getSingletons.foldLeft(ListBuffer[Any]()) {
      (dwSingleton, em) =>
        if (em.isInstanceOf[ExceptionMapper[_]] && em.getClass.getName.startsWith("com.yammer.dropwizard.jersey.")) {
          dwSingleton += em
        }
        dwSingleton
    }.toList
    jrConfig.getSingletons.removeAll(dwSingletonsToRemove)
  }
}

trait MicroService[T <: Configuration] extends Service[T] with CustomExceptionMappers with Logging {

  import com.yammer.dropwizard.bundles.ScalaBundle
  import com.yammer.metrics.core.HealthCheck
  import com.yammer.dropwizard.lifecycle.Managed
  import uk.gov.gds.microservice.dropwizard.rest.{ GenericErrorResponseMapper, FailureResponseMapper }

  val applicationName: String

  val genericExceptionMappers: List[ExceptionMapper[_]] = List[ExceptionMapper[_]](new SuccessResponseMapper, new FailureResponseMapper, new GenericErrorResponseMapper)

  def initialize(bootstrap: Bootstrap[T]) {
    bootstrap.setName(applicationName)
    bootstrap.addBundle(new ScalaBundle)
    bootstrap.getObjectMapperFactory.registerModule(new CustomJodaTimeModule)
  }

  def run(configuration: T, environment: Environment) {
    info(s"Starting application '$applicationName' with config ${configuration.toString}")

    addMappers(environment, genericExceptionMappers)
  }

  protected def bundles(bootstrap: Bootstrap[T], bundles: List[Bundle]) {
    bundles.foreach(bootstrap.addBundle(_))
  }

  protected def resources(environment: Environment, resources: List[Any]) {
    resources.foreach { environment.addResource(_) }
  }

  protected def healthChecks(environment: Environment, healthChecks: List[HealthCheck]) {
    healthChecks.foreach(environment.addHealthCheck(_))
  }

  protected def managed(environment: Environment, managed: Managed*) {
    managed.foreach(environment.manage(_))
  }

  protected def providers(environment: Environment, providers: List[Any]) {
    providers.foreach(environment.addProvider(_))
  }

  def makeHttpClient(configuration: ClientConfiguration, environment: Environment): Client = {

    val clientBuilder = new JerseyClientBuilder().using(environment).using(configuration.httpClient)

    if (!configuration.httpClient.sslPeersValidation) {

      val registry = new SchemeRegistry()
      registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory))
      registry.register(new Scheme("https", 443, new SSLSocketFactory(allowAllCertsTrustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)))
      clientBuilder.using(registry)
    }

    clientBuilder.build()
  }
}

private object allowAllCertsTrustStrategy extends TrustStrategy {
  def isTrusted(chain: Array[X509Certificate], authType: String): Boolean = true
}

case class ServiceKey(val name: String)

trait ServiceClients[A <: Configuration with ServiceMappings] {
  self: MicroService[A] =>

  implicit def formats = JsonExtraction.formats

  def createKeyedClients(client: Client, microServicesConfigs: A) {
    clients(ServiceClient(Some(microServicesConfigs.serviceMappings.map(m => m._1 -> m._2.url)), client))
  }

  object clients {

    private var er: Option[Map[ServiceKey, ServiceClient]] = None

    def apply(externalResources: Option[Map[ServiceKey, ServiceClient]]) {
      er = externalResources
    }

    def apply(key: ServiceKey): Option[ServiceClient] = if (er.isEmpty) throw new RuntimeException("The container has been mis-configured") else er.get.get(key)
  }

}

class FaviconBundle extends AssetsBundle("/assets/favicon.ico", "/favicon.ico")
