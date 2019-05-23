package com.example.services.opc

import com.example.UnitSpec
import com.example.util.ClientUtil.FutureO
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.enumerated.{ ServerState, TimestampsToReturn }
import wvlet.log.Logger

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters

class OpcClientSpec extends UnitSpec {

  private val logger = Logger.of[OpcClientSpec]

  "OpcUaClient#createClient" should "create a new client instance against a OPC UA Server" in {
    Await.result(OpcClient(appCfg.opcCfg).createClient, Duration.Inf) match {
      case Some(opcUaClient) =>
        // Connect to the server
        val client = Await.result(FutureConverters.toScala(opcUaClient.connect()), Duration.Inf)

        // Read some data synchronously
        val node = client.getAddressSpace.createVariableNode(Identifiers.Server_ServerStatus_StartTime)
        val value = node.readValue.get
        logger.info(s"StartTime = ${value.getValue.getValue}")

        // Let us read some data
        val nodeIds = Seq(Identifiers.Server_ServerStatus_State, Identifiers.Server_ServerStatus_CurrentTime)
        val result = Await.result(
          FutureConverters.toScala(opcUaClient.readValues(0.0, TimestampsToReturn.Both, nodeIds.asJava)),
          Duration.Inf).asScala
        val v0 = result.head
        val v1 = result.last
        logger.info(s"State = ${ServerState.from(v0.getValue.getValue.asInstanceOf[Integer])}")
        logger.info(s"CurrentTime = ${v1.getValue.getValue}")

      case _ => fail("shit failed....")
    }
  }
}
