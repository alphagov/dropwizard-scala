package uk.gov.gds.microservice.dropwizard.conf

import com.sun.jersey.api.client.Client
import com.yammer.dropwizard.lifecycle.Managed
import uk.gov.gds.microservice.mongo.MongoConnector

object ConfigImplicits {

  implicit def managedMongo(mongo: MongoConnector) = new ManagedMongo(mongo)

  implicit def managedHttpClient(httpClient: Client) = new ManagedHttpClient(httpClient)

}

