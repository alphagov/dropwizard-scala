package uk.gov.gds.microservice.dropwizard.rest

import com.yammer.metrics.core.HealthCheck
import uk.gov.gds.microservice.mongo.MongoConnector
import com.yammer.metrics.core.HealthCheck.Result
import scala.util.{Failure, Success, Try}

class MongoHealthCheck(mongo: MongoConnector, name: String = "mongo") extends HealthCheck(name) {
  def check(): Result = {
    Try(mongo.db()("any").size) match {
      case Success(_) => Result.healthy()
      case Failure(e) => Result.unhealthy(e)
    }
  }
}
