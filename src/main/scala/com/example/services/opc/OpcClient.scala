package com.example.services.opc

import com.example.config.OpcConfig
import org.eclipse.milo.opcua.sdk.client
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig
import wvlet.log.Logger
import org.eclipse.milo.opcua.stack.client.DiscoveryClient
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription

import scala.compat.java8.FutureConverters
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

// TODO: For the time being, we use this default global thread pool
import scala.concurrent.ExecutionContext.Implicits.global


final class OpcClient(opcConfig: OpcConfig) {

  private val logger = Logger.of[OpcClient]
  private val opcServerEndpointURL = opcConfig.serverEndPointUrl

  def createClient: Future[Option[client.OpcUaClient]] = {

    // TODO: We can deal with certificates later
    //val securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "security")
    //Files.createDirectories(securityTempDir)
    //if (!Files.exists(securityTempDir)) throw new Nothing("unable to create security dir: " + securityTempDir)
    //val loader = new Nothing().load(securityTempDir)
    //val securityPolicy = clientExample.getSecurityPolicy

    def endpointFilter: EndpointDescription => Boolean = _ => true

    def endPointsF: Future[List[EndpointDescription]] = {
      FutureConverters.toScala(Try(DiscoveryClient.getEndpoints(opcServerEndpointURL)) match {
        case Success(urls) => urls
        case Failure(fail) =>
          logger.warn(s"TODO..... Proper log message ${fail.getMessage}")
          // try the explicit discovery endpoint as well
          val discoveryUrl = if (!opcServerEndpointURL.endsWith("/")) {
            s"$opcServerEndpointURL/discovery"
          } else s"${opcServerEndpointURL}discovery"

          logger.info(s"Trying explicit discovery URL: $discoveryUrl")
          DiscoveryClient.getEndpoints(discoveryUrl)
      }).map(_.asScala.toList)
    }

    endPointsF.map { endPoints =>
      endPoints.toStream
        //.filter(e => e.getSecurityPolicyUri.equals(securityPolicy.getSecurityPolicyUri))
        .find(endpointFilter)
        .map(endpoint => {
          logger.info(s"Using endpoint: ${endpoint.getEndpointUrl}, ${endpoint.getSecurityMode}")
          val config = OpcUaClientConfig.builder()
            .setApplicationName(LocalizedText.english("OPC Streamer Client Application"))
            .setApplicationUri("urn:eclipse:milo:examples:client")
            .setEndpoint(endpoint)
            // No need to worry about certificates right now!
            //.setCertificate(certs.clientCert())
            //.setKeyPair(certs.clientKeyPair())
            //.setIdentityProvider(opcClient.identifyProvider)
            .setRequestTimeout(uint(5000))
            .build()

          OpcUaClient.create(config)
        })
    }
  }
}
object OpcClient {

  def apply(opcConfig: OpcConfig) =
    new OpcClient(opcConfig)
}