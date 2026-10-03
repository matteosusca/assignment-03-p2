package it.unibo.agar.network

import com.rabbitmq.client.{Channel, Connection}
import it.unibo.agar.protocol.{PlayerCommand, ProtocolCodec, WorldSnapshot}

class ClientNetworkAdapter(
  connection: Connection,
  onWorldSnapshotReceived: WorldSnapshot => Unit
) extends AutoCloseable:

  private val consumerChannel: Channel = connection.createChannel()
  private val publisherChannel: Channel = connection.createChannel()
  RabbitMQConfig.setupTopology(consumerChannel)

  // temporary queue
  private val privateQueue: String = consumerChannel.queueDeclare().getQueue()
  consumerChannel.queueBind(privateQueue, RabbitMQConfig.WorldExchange, "")

  // receive world snapshots from the server
  consumerChannel.basicConsume(
    privateQueue,
    true,
    (_, delivery) => ProtocolCodec.decode[WorldSnapshot](delivery.getBody).foreach(onWorldSnapshotReceived),
    _ => ()
  )

  // command send to centralized server queue
  def sendCommand(command: PlayerCommand): Unit =
    publisherChannel.basicPublish("", RabbitMQConfig.CommandsQueue, null, ProtocolCodec.encode(command))

  def close(): Unit =
    if consumerChannel.isOpen then consumerChannel.close()
    if publisherChannel.isOpen then publisherChannel.close()