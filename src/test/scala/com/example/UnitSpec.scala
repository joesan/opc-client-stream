package com.example

import com.example.config.AppConfig
import org.scalatest._

abstract class UnitSpec extends FlatSpec with Matchers with OptionValues with Inside with Inspectors {

  // For all unit tests, we use the application.test.conf configuration
  System.setProperty("env", "test")
  val appCfg = AppConfig.load()
}