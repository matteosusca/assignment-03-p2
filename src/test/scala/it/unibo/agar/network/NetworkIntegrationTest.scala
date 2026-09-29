package it.unibo.agar.network

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import it.unibo.agar.protocol.*
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

class NetworkIntegrationTest extends AnyFunSuite with Matchers:

	test("Client should send command to Server and Server should broadcast to all Clients"):
		val connection = RabbitMQConfig.connect()

		val receivedCommands = new LinkedBlockingQueue[PlayerCommand]()
		val client1Snapshots = new LinkedBlockingQueue[WorldSnapshot]()
		val client2Snapshots = new LinkedBlockingQueue[WorldSnapshot]()

		val server = ServerNetworkAdapter(connection, receivedCommands.offer)
		val client1 = ClientNetworkAdapter(connection, client1Snapshots.offer)
		val client2 = ClientNetworkAdapter(connection, client2Snapshots.offer)

		// 1. Client 1 sends a command
		val moveCmd = PlayerCommand.Move("p1", 1.0, 0.0)
		client1.sendCommand(moveCmd)
		receivedCommands.poll(3, TimeUnit.SECONDS) shouldBe moveCmd

		// 2. Server broadcasts a world snapshot
		val snapshot = WorldSnapshot(1L, Seq(PlayerSnapshot("p1", 10, 20, 5, 50)), Seq.empty, false)
		server.broadcastWorldSnapshot(snapshot)

		// 3. Both clients should receive the snapshot
		client1Snapshots.poll(3, TimeUnit.SECONDS) shouldBe snapshot
		client2Snapshots.poll(3, TimeUnit.SECONDS) shouldBe snapshot

		// Clean up
		client1.close()
		client2.close()
		server.close()
		connection.close()