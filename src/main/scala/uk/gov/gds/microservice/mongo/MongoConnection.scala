package uk.gov.gds.microservice.mongo

import com.mongodb.casbah.{ WriteConcern, MongoDB, MongoURI }
import scala.{ Left, Right }

class MongoConnector(val mongoConnectionUri: String) extends MongoConnection

trait MongoConnection {

  val mongoConnectionUri: String

  val writeConcern = WriteConcern.Safe

  implicit def db: () => MongoDB = () => dbc

  private lazy val dbc = connect

  private def connect = {
    val mongoURI = MongoURI(mongoConnectionUri)
    mongoURI.connectDB match {
      case Right(s) => {
        s.setWriteConcern(writeConcern)
        s
      }
      case Left(e) => throw e
    }
  }

  def close() { db().underlying.getMongo.close() }
}
