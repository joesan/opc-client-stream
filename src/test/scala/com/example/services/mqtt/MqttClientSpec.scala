package com.example.services.mqtt

import com.example.UnitSpec
import com.example.config.MqttConfig
import com.example.services.mqtt.MqttClient.MqttSubscriber
import org.eclipse.paho.client.mqttv3.{ IMqttDeliveryToken, MqttCallback, MqttConnectOptions, MqttMessage, MqttClient => MqttPahoClient }
import monix.eval.Task
import monix.execution.Cancelable
import monix.execution.cancelables.BooleanCancelable
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await

class MqttClientSpec extends UnitSpec with BeforeAndAfterAll {

  def retryBackoff(source: Task[MqttPahoClient], firstDelay: FiniteDuration, nextDelay: FiniteDuration): Task[MqttPahoClient] = {
    source.onErrorHandleWith {
      case _: Exception =>
        if (firstDelay == nextDelay) {
          println("First Retry...")
          retryBackoff(source, firstDelay, nextDelay.plus(2.seconds))
        } // This means, I want to retry immediately
        else if (nextDelay.minus(firstDelay) <= 60.seconds) {
          println(s"2 seconds retry...retrying for the last ${nextDelay.minus(firstDelay)}")
          retryBackoff(source, firstDelay, nextDelay + 2.seconds).delayExecution(firstDelay)
        } // This means, I want to retry after a certain interval
        else {
          println(s"2 minutes retry...retrying for the last ${nextDelay.minus(firstDelay).toMinutes} minute")
          retryBackoff(source, firstDelay, nextDelay + 2.seconds).delayExecution(firstDelay)
        } // This means, I have to now retry every 2 minutes
    }
  }

  def connect() = {
    val mqttConnectOptions = new MqttConnectOptions()
    mqttConnectOptions.setCleanSession(true)
    mqttConnectOptions.setAutomaticReconnect(true)
    // 0. Make the connection to the Mqtt broker
    val mqttConnection = Task {
      val persistence = new MemoryPersistence
      val mqttClient = new MqttPahoClient(s"tcp://localhost:1883", MqttPahoClient.generateClientId, persistence)
      mqttClient.connect(mqttConnectOptions)
      //mqttClient.setCallback(mqttClient.)
      mqttClient
    }

    // 1. Check if the connection is successful, if not retry
    mqttConnection.materialize.flatMap {
      case Success(succ) =>
        // 3. Run the task
        Task.now(succ)
      case Failure(fail) =>
        System.err.println(s"Failure when trying to connect to Mqtt Broker because of ${fail.getMessage}")
        // 4. If failures happen when connecting, retry with an initial delay of 2 seconds
        retryBackoff(mqttConnection, 2.seconds, 2.seconds)
    }
  }

  "connect" should "connect" in {
    val mqttClientTask = connect()
    val result = Await.result(mqttClientTask.runToFuture, Duration.Inf)
    println(result.getServerURI)
  }

  "connect with Observable" should "connect to mqtt" in {
    val mqttCfg = MqttConfig(s"tcp://localhost:1883", "state")
    //val observable = new MqttObservable[String](mqttCfg, new MqttObserver[String])
    val subscriber = new MqttObserver[String]
    // This should do the trick!
    //observable.unsafeSubscribeFn(subscriber)
  }
}
