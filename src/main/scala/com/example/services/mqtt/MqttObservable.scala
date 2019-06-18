package com.example.services.mqtt

import com.example.config.MqttConfig
import org.eclipse.paho.client.mqttv3.{ IMqttDeliveryToken, MqttCallback, MqttConnectOptions, MqttMessage, MqttClient => MqttPahoClient }
import monix.eval.Task
import monix.execution.{ Cancelable, Scheduler }
import monix.execution.cancelables.{ BooleanCancelable, SingleAssignCancelable }
import monix.reactive.observables.ConnectableObservable
import monix.reactive.observers.Subscriber
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import wvlet.log.Logger

import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import monix.execution.Scheduler.Implicits.global
import monix.reactive.OverflowStrategy.Unbounded
import monix.reactive.subjects.ConcurrentSubject

final class MqttObservable[String](val cfg: MqttConfig)
  extends ConnectableObservable[String] {

  private val logger = Logger.of[MqttObservable[String]]

  // Handles connection close / dispose
  private[this] val connection = SingleAssignCancelable()

  // Channel that pipes message from Mqtt broker into this MqttObservable
  private[this] val publishChannel = ConcurrentSubject.publish[String](Unbounded)

  override def unsafeSubscribeFn(subscriber: Subscriber[String]): Cancelable = {
    publishChannel.takeWhileNotCanceled(connection)
      .unsafeSubscribeFn(subscriber)
  }

  // exponential backoff with a fixed delay after about 2 minutes
  // When I connect for the first time, if the service is unavailable, I want to retry connecting
  // Retry connections should happen after a fixed interval
  // TODO: under test....
  private def retryBackoff(source: Task[MqttPahoClient], firstDelay: FiniteDuration, nextDelay: FiniteDuration): Task[MqttPahoClient] = {
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

  val callback: MqttCallback = new MqttCallback {
    override def connectionLost(cause: Throwable): Unit = {
      publishChannel.onError(cause)
      logger.info(cause)
    }
    override def deliveryComplete(token: IMqttDeliveryToken): Unit = {

    }
    override def messageArrived(topic: Predef.String, message: MqttMessage): Unit = {
      publishChannel.onNext(message.getPayload.asInstanceOf[String])
      logger.info("Using Default Console Callback --> Receiving Data, Topic : %s, Message : %s".format(topic, message))
    }
  }

  override def connect(): Cancelable = {
    // 0. Make the connection to the Mqtt broker
    val mqttConnectOptions = new MqttConnectOptions()
    mqttConnectOptions.setCleanSession(true)
    mqttConnectOptions.setAutomaticReconnect(true)
    val mqttConnection = Task {
      val persistence = new MemoryPersistence
      val mqttClient = new MqttPahoClient(s"tcp://localhost:1883", MqttPahoClient.generateClientId, persistence)
      mqttClient.connect(mqttConnectOptions)
      mqttClient.setCallback(callback)
      mqttClient
    }

    // 1. Check if the connection is successful, if not retry
    val materializedTask = mqttConnection.materialize.flatMap {
      case Success(succ) =>
        // 2. Register a callback that says what to do when we disconnect
        connection := BooleanCancelable { () =>
          succ.disconnect()
        }
        // 3. Run the task
        Task.now(succ)
      case Failure(fail) =>
        logger.error(s"Failure when trying to connect to Mqtt Broker because of ${fail.getMessage}")
        // 4. If failures happen when connecting for the first time, retry with an initial delay of 2 seconds
        retryBackoff(mqttConnection, 2.seconds, 2.seconds)
    }

    // 5. Here is where we run the Task
    materializedTask.runToFuture

    connection
  }
}
