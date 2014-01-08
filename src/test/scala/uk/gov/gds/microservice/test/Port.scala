package uk.gov.gds.microservice.test

import annotation.tailrec
import java.net.ServerSocket
import uk.gov.gds.microservice.utils.Logging

object Port extends Logging {
  val rnd = new scala.util.Random
  val range = (8000 to 8999)

  @tailrec
  def randomAvailable: Int = {
    range(rnd.nextInt(range length)) match {
      case 8080 => randomAvailable
      case 8090 => randomAvailable
      case p: Int => {
        available(p) match {
          case false => {
            debug(s"Port $p is in use, trying another")
            randomAvailable
          }
          case true => {
            debug("Taking port : " + p)
            p
          }
        }
      }
    }
  }

  private def available(p: Int): Boolean = {
    var socket: ServerSocket = null
    try {
      socket = new ServerSocket(p)
      socket.setReuseAddress(true)
      true
    } catch {
      case t: Throwable => false
    } finally {
      if (socket != null) socket.close()
    }
  }
}
