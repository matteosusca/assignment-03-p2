package it.unibo.agar.network

import com.rabbitmq.client.{BuiltinExchangeType, Channel, Connection, ConnectionFactory}

object RabbitMQConfig:
	val CommandsQueue: String = "agar.commands"
	val WorldExchange: String = "agar.world.fanout"

	def connect(host: String = "localhost"): Connection =
		val factory = ConnectionFactory()
		factory.setHost(host)
		factory.newConnection()

	def setupTopology(channel: Channel): Unit =
		channel.queueDeclare(CommandsQueue, true, false, false, null)
		channel.exchangeDeclare(WorldExchange, BuiltinExchangeType.FANOUT)