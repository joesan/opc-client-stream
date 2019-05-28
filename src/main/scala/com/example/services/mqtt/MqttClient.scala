package com.example.services.mqtt

import com.example.config.MqttConfig
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.net.PemKeyCertOptions
import io.vertx.scala.mqtt.{MqttClient, MqttClientOptions}

import scala.util.{Failure, Success}

// TODO: For the time being, we use this default global thread pool
import scala.concurrent.ExecutionContext.Implicits.global


class MqttClient(cfg: MqttConfig) {

  var options = MqttClientOptions()
    .setPemKeyCertOptions(PemKeyCertOptions()
      .setKeyPath("./src/test/resources/tls/server-key.pem")
      .setCertPath("./src/test/resources/tls/server-cert.pem"))
    .setSsl(true)

  var mqttClient = MqttClient.create(Vertx.vertx(), options)
  mqttClient.connectFuture(cfg.port, cfg.host).onComplete {
    case Success(result) => println("Success")
    case Failure(cause) => println("Failure")
  }
}
