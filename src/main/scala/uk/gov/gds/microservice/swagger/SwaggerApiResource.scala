package uk.gov.gds.microservice.swagger

import com.wordnik.swagger.annotations.Api

import javax.ws.rs.Path
import javax.ws.rs.Produces
import com.wordnik.swagger.jaxrs.listing.{ ApiListing, ApiListingResource }

@Path("/resources")
@Api("/resources")
@Produces(Array("application/json"))
class SwaggerApiResource extends ApiListing {
}
