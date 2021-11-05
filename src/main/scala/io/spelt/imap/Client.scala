package io.spelt.imap

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.stream.scaladsl.{Flow, Tcp}
import akka.stream.scaladsl.Tcp.OutgoingConnection
import akka.util.ByteString

import scala.concurrent.Future
import scala.language.reflectiveCalls

object Client {
  type StructuralTcp = {
    def outgoingConnection(host: String, port: Int): Flow[ByteString, ByteString, Future[OutgoingConnection]]
  }

  // inbound messages
  trait InboundProtocol
  case class Connect(hostname: String, port: Int, sender: ActorRef[Connected], tcpConnector: Option[StructuralTcp] = None) extends InboundProtocol

  // outbound messages
  case class Connected(hostname: String, port: Int)

  /**
   * Creates a Client
   *
   * The caller's ActorSystem that is used to spawn a Client will be available
   * in the context from Behaviors.receive.
   *
   * @return
   */
  def apply(): Behavior[InboundProtocol] = Behaviors.receive { (context, message) =>
    message match {
      case Connect(hostname, port, sender, None) =>
        connect(hostname, port, sender, Tcp()(context.system))
      case Connect(hostname, port, sender, Some(tcp)) =>
        connect(hostname, port, sender, tcp)
    }
  }

  private def connect(hostname: String, port: Int, sender: ActorRef[Connected], tcp: StructuralTcp): Behavior[InboundProtocol] = {
    tcp.outgoingConnection(hostname, port)

    sender ! Connected(hostname, port)
    Behaviors.same
  }
}
