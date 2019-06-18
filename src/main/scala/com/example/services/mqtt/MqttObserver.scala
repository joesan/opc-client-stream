package com.example.services.mqtt

import monix.execution.{ Ack, Scheduler }
import monix.execution.Ack.Continue
import monix.reactive.Observer
import monix.reactive.observers.Subscriber
import wvlet.log.Logger

import scala.concurrent.Future

class MqttObserver[String] extends Observer[String] {

  private val logger = Logger.of[MqttObserver[String]]

  override def onNext(elem: String): Future[Ack] = {
    logger.info(s"Got a message from Mqtt broker $elem")
    Continue
  }

  override def onError(ex: Throwable): Unit = {
    logger.error(s"Stream error happened ${ex.getMessage}")
  }

  override def onComplete(): Unit = {
    logger.info(s"Stream ended")
  }
}
