package io.spelt.imap

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Tcp.OutgoingConnection
import akka.util.ByteString
import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.Future

class ClientSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with MockFactory {
  trait MockTcp {
    def outgoingConnection(host: String, port: Int): Flow[ByteString, ByteString, Future[OutgoingConnection]]
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "Unconnected state" when {
    "Connect" must {
      "connect to the host and respond with Connected" in {
        val client = testKit.spawn(Client(), "client")
        val probe = testKit.createTestProbe[Client.Connected]()
        val tcpStub = mock[MockTcp]
        val hostname = "imap.example.com"
        val port = 143

        (tcpStub.outgoingConnection _).expects(hostname, port) // .returns()
        client ! Client.Connect(hostname, port, probe.ref, Some(tcpStub))

        probe.expectMessage(Client.Connected(hostname, port))
      }
    }
  }
}
