package com.example.services.mqtt

import com.example.config.MqttConfig
import org.eclipse.paho.client.mqttv3.{ IMqttDeliveryToken, MqttCallback, MqttMessage }
import org.eclipse.paho.client.mqttv3.{ MqttClient => MqttPahoClient }
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import wvlet.log.Logger

import scala.util.{ Failure, Success, Try }

trait MqttClient {

  def client: Try[MqttPahoClient]
}
object MqttClient {

  final class MqttSubscriber(cfg: MqttConfig, someCallback: Option[MqttCallback] = None) extends MqttClient {

    private val logger = Logger.of[MqttSubscriber]

    val callback: MqttCallback = someCallback.getOrElse(new MqttCallback {
      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        logger.info("Using Default Console Callback --> Receiving Data, Topic : %s, Message : %s".format(topic, message))
      }
      override def connectionLost(cause: Throwable): Unit = {
        logger.info(cause)
      }
      override def deliveryComplete(token: IMqttDeliveryToken): Unit = {

      }
    })

    def client: Try[MqttPahoClient] = Try {
      val persistence = new MemoryPersistence
      new MqttPahoClient(s"${cfg.url}", MqttPahoClient.generateClientId, persistence)
      // TODO: Remove the lines below as Connection happens in the Observable
      //client.connect()
      //client
    }
  }
}