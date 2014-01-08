package uk.gov.gds.microservice.auth

import com.sun.jersey.spi.inject.{ Injectable, InjectableProvider }
import com.sun.jersey.api.model.Parameter
import com.sun.jersey.core.spi.component.{ ComponentContext, ComponentScope }
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable
import com.sun.jersey.api.core.HttpContext
import javax.ws.rs.core.{ Response, HttpHeaders }
import javax.ws.rs.WebApplicationException
import java.lang.annotation.Annotation

class AuthorisationProvider[A <: Annotation, T >: Null](authoriser: Authoriser,
    realm: String) extends InjectableProvider[A, Parameter] {

  @Override
  def getScope: ComponentScope = ComponentScope.PerRequest

  @Override
  def getInjectable(cc: ComponentContext, a: A, p: Parameter): Injectable[T] =
    new AuthorisationInjectable[A, T](authoriser, a, realm)
}

class AuthorisationInjectable[A <: Annotation, T >: Null](authoriser: Authoriser,
    annotation: A,
    realm: String) extends AbstractHttpContextInjectable[T] {

  private[auth] def getAnnotationData(annotation: A) = {
    val c = annotation.getClass
    (c.getMethod("value").invoke(annotation).asInstanceOf[String], c.getMethod("regime").invoke(annotation).asInstanceOf[String])
  }

  def getValue(ctx: HttpContext): T = {
    try {
      val header = Option(ctx.getRequest.getHeaderValue(HttpHeaders.AUTHORIZATION))

      if (header.isDefined) {
        val pair = header.get.split(" ")

        pair match {
          case Array("Bearer", credentials) => {
            val annotationData = getAnnotationData(annotation)
            val segments = ctx.getUriInfo.getPathSegments(annotationData._1)

            if (segments.size == 1) {
              val result = authoriser.authorise(AuthorisationRequest(credentials, segments.get(0).getPath, annotationData._2))

              if (result.isPresent)
                result.get
            }
          }
          case _ => throw new WebApplicationException(Response.Status.UNAUTHORIZED)
        }
      }
    } catch {
      case _: Exception => throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR)
    }
    null
  }
}
