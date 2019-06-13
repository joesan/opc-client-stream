package com.example.services.opc

import com.example.UnitSpec
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.enumerated.{ ServerState, TimestampsToReturn }
import org.scalatest.BeforeAndAfterAll
import wvlet.log.Logger

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters
import scala.util.control.NonFatal
import scala.util.{ Failure, Success }


class OpcClientSpec extends UnitSpec with BeforeAndAfterAll {

  private val logger = Logger.of[OpcClientSpec]

  // This OPC client will be used globally by all the tests in this spec
  private val opcClient = try {
    Await.result(OpcClient(appCfg.opcCfg).createClient, Duration.Inf) match {
      case Some(opcUaClient) =>
        // Connect to the server
        Await.result(FutureConverters.toScala(opcUaClient.connect()), Duration.Inf)
        Success(opcUaClient)
      case None => Failure(new Exception("Unable to connect to the OPC server"))
    }
  } catch {
    case NonFatal(ex) => Failure(ex)
  }

  override def afterAll() = {
    opcClient.map(_.disconnect())
    logger.info(s"Successfully disconnected from OPC server @ ${appCfg.opcCfg.serverEndPointUrl}")
  }

  "OpcUaClient#createClient" should "create a new client instance against a OPC UA Server" in {
    opcClient match {
      case Success(client) =>
        appCfg.opcCfg.serverEndPointUrl === client.getConfig.getEndpoint.getEndpointUrl
        logger.info(s"Successfully connected to the OPC server @ ${appCfg.opcCfg.serverEndPointUrl}")
      case Failure(ex) => fail(s"Unable to create OpcUaClient because of ${ex.getMessage}")
    }
  }

  "OpcUaClient#readServerStatus" should "create a new client instance against a OPC UA Server" in {
    opcClient match {
      case Success(client) =>
        // Let us read some data
        val nodeIds = Seq(Identifiers.Server_ServerStatus_State, Identifiers.Server_ServerStatus_CurrentTime)
        val result = Await.result(
          FutureConverters.toScala(client.readValues(0.0, TimestampsToReturn.Both, nodeIds.asJava)),
          Duration.Inf).asScala
        val v0 = result.head
        val v1 = result.last
        logger.info(s"State = ${ServerState.from(v0.getValue.getValue.asInstanceOf[Integer])}")
        logger.info(s"CurrentTime = ${v1.getValue.getValue}")

      case Failure(ex) => fail(s"Unable to create OpcUaClient because of ${ex.getMessage}")
    }
  }

  "OpcUaClient#browseNodes" should "create a new client instance against a OPC UA Server" in {
    opcClient match {
      case Success(client) =>
        browseNode("", client, Identifiers.RootFolder)

      case Failure(ex) => fail(s"Unable to create OpcUaClient because of ${ex.getMessage}")
    }
  }

  private def browseNode(indent: String, client: OpcUaClient, browseRoot: NodeId): Unit = {
    try {
      val nodes = Await.result(
        FutureConverters.toScala(client.getAddressSpace.browse(browseRoot)),
        Duration.Inf).asScala
      for (node <- nodes) {
        logger.info(s"$indent, ${node.getBrowseName.get.getName}")
        // recursively browse to children
        browseNode(indent + "  ", client, node.getNodeId.get())
      }
    } catch {
      case NonFatal(ex) =>
        logger.error("Browsing nodeId={} failed: {}", browseRoot, ex.getMessage)
    }
  }
}
