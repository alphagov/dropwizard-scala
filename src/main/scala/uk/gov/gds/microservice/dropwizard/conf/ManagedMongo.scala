package uk.gov.gds.microservice.dropwizard.conf

import com.yammer.dropwizard.lifecycle.Managed
import uk.gov.gds.microservice.mongo.MongoConnector

class ManagedMongo(mongo: MongoConnector) extends Managed {

  override def start() {
  }

  override def stop() {
    mongo.close()
  }
}