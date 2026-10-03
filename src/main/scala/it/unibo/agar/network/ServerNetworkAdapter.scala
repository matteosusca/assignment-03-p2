package it.unibo.agar.network

import com.rabbitmq.client.{Channel, Connection}
import it.unibo.agar.protocol.{PlayerCommand, ProtocolCodec, WorldSnapshot}

class ServerNetworkAdapter(
  connection: Connection,
  onCommandReceived: PlayerCommand => Unit
) extends AutoCloseable:

  private val consumerChannel: Channel  = connection.createChannel()
  private val publisherChannel: Channel = connection.createChannel()
  RabbitMQConfig.setupTopology(consumerChannel)

  // point-to-point queue listening for commands from clients
  consumerChannel.basicConsume(
    RabbitMQConfig.CommandsQueue,
    true,
    (_, delivery) => ProtocolCodec.decode[PlayerCommand](delivery.getBody).foreach(onCommandReceived),
    _ => ()
  )

  // broadcast publish/subscribe exchange for world snapshots
  def broadcastWorldSnapshot(snapshot: WorldSnapshot): Unit =
    val body = ProtocolCodec.encode(snapshot)
    publisherChannel.basicPublish(RabbitMQConfig.WorldExchange, "", null, body)

  def close(): Unit = {
    consumerChannel.close()
    publisherChannel.close()
  }
