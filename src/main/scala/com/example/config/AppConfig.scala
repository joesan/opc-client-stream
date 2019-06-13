package com.example.config

import com.typesafe.config.Config

/**
  * Type-safe configuration used throughout the application.
  */
final case class AppConfig(
  environment: String,
  appName: String,
  appHost: String,
  appPort: Int,
  opcCfg: OpcConfig
)
final case class OpcConfig(
  serverEndPointUrl: String
)
final case class MqttConfig(
  url: String,
  topic: String,
  clientId: String = "default-client-id",
  persistenceDir: Option[String]
)
final case class KafkaConfig(
  servers: String,
  topics: String,
  groupId: String)
object AppConfig {

  def load(): AppConfig =
    load(ConfigUtil.loadFromEnv())

  def load(config: Config): AppConfig = {
    AppConfig(
      environment = config.getString("environment"),
      appName = config.getString("appName"),
      appHost = Option(config.getString("appHost")).getOrElse("localhost"),
      appPort = Option(config.getInt("appPort")).getOrElse(8080),
      opcCfg = OpcConfig(
        serverEndPointUrl = Option(config.getString("opc.server.url")).getOrElse("localhost:8080"),
      )
    )
  }
}