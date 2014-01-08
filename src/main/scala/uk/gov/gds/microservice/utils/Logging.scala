package uk.gov.gds.microservice.utils

import org.slf4j.LoggerFactory

trait Logging {
  private lazy val logger = LoggerFactory.getLogger(this.getClass)

  def debug(msg: => String) { if (logger.isDebugEnabled) logger.debug(msg) }

  def debug(msg: => String, t: Throwable) { if (logger.isDebugEnabled) logger.debug(msg, t) }

  def info(msg: => String) { if (logger.isInfoEnabled) logger.info(msg) }

  def info(msg: => String, t: Throwable) { if (logger.isInfoEnabled) logger.info(msg, t) }

  def warn(msg: => String) { if (logger.isWarnEnabled) logger.warn(msg) }

  def warn(msg: => String, t: Throwable) { if (logger.isWarnEnabled) logger.warn(msg, t) }

  def trace(msg: => String) { if (logger.isTraceEnabled) logger.trace(msg) }

  def trace(msg: => String, t: Throwable) { if (logger.isTraceEnabled) logger.trace(msg, t) }

  def error(msg: => String) { if (logger.isErrorEnabled) logger.error(msg) }

  def error(msg: => String, t: Throwable) { if (logger.isErrorEnabled) logger.error(msg, t) }
}