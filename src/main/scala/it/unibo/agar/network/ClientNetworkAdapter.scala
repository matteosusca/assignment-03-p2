package it.unibo.agar.network

import com.rabbitmq.client.{Channel, Connection}
import it.unibo.agar.protocol.{PlayerCommand, ProtocolCodec, WorldSnapshot}

class ClientNetworkAdapter(
														connection: Connection,
														onWorldSnapshotReceived: WorldSnapshot => Unit
													) extends AutoCloseable:

	private val channel: Channel = connection.createChannel()
	RabbitMQConfig.setupTopology(channel)

	// temporary queue
	private val privateQueue: String = channel.queueDeclare().getQueue()
	channel.queueBind(privateQueue, RabbitMQConfig.WorldExchange, "")

	// receive world snapshots from the server
	channel.basicConsume(
		privateQueue,
		true,
		(_, delivery) =>
			ProtocolCodec.decode[WorldSnapshot](delivery.getBody).foreach(onWorldSnapshotReceived),
		_ => ()
	)

	// command send to centralized server queue
	def sendCommand(command: PlayerCommand): Unit =
		channel.basicPublish("", RabbitMQConfig.CommandsQueue, null, ProtocolCodec.encode(command))

	def close(): Unit = channel.close()
