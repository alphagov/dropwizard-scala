package uk.gov.gds.microservice.rest

import uk.gov.gds.microservice.test.BaseSpec
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.gds.microservice.dropwizard.rest.MongoHealthCheck
import uk.gov.gds.microservice.mongo.MongoConnector
import org.scalatest.BeforeAndAfter
import com.yammer.metrics.core.HealthCheck.Result
import com.mongodb.casbah.{MongoCollection, MongoDB}

class MongoHealthCheckSpec extends BaseSpec with MockitoSugar with BeforeAndAfter {

  val mockConnector  = mock[MongoConnector]
  val mockMongoDb    = mock[MongoDB]
  val mockCollection = mock[MongoCollection]

  before {
    reset(mockConnector)
    when(mockConnector.db).thenReturn(() => mockMongoDb)
  }

  "Mongo HealthCheck" should {
    "return a healthy result when mongo is fine" in {

      when(mockMongoDb("any")).thenReturn(mockCollection)
      when(mockMongoDb("any").size).thenReturn(0)

      val healthCheck = new MongoHealthCheck(mockConnector)
      healthCheck.check() must be (Result.healthy())
    }

    "return an unhealthy result when mongo is down" in {
      val exception = new RuntimeException("Test Exception")
      when(mockMongoDb("any")).thenThrow(exception)
      val healthCheck = new MongoHealthCheck(mockConnector)
      healthCheck.check() must be (Result.unhealthy(exception))
    }
  }
}
