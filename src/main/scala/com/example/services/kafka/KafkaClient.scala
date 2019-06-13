package com.example.services.kafka

import com.example.config.KafkaConfig
import monix.eval.Task
import monix.kafka.{ CommittableOffsetBatch, KafkaConsumerConfig, KafkaConsumerObservable }

import scala.concurrent.duration._

final class KafkaClient(kafkaCfg: KafkaConfig) {

  val consumerCfg = KafkaConsumerConfig.default.copy(
    bootstrapServers = List(kafkaCfg.servers),
    groupId = kafkaCfg.groupId)

  val observable =
    KafkaConsumerObservable.manualCommit[String, String](consumerCfg, List(kafkaCfg.topics))
      .map(message =>
        (message.record.value(), message.committableOffset, message.record.topic()))
      .mapEval { case (value, offset, topic) => performBusinessLogic(value, topic).map(_ => offset) }
      .bufferTimedAndCounted(1.second, 1000)
      .mapEval(offsets => CommittableOffsetBatch(offsets).commitSync())

  def performBusinessLogic(str: String, topic: String): Task[Unit] = {
    val a = Some(1)
    a.map(elem => Some(elem))
    Task { println(s"From topic $topic Got message $str") }
  }
}
