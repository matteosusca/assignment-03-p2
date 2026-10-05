package it.unibo.agar.client

import it.unibo.agar.network.{ClientNetworkAdapter, RabbitMQConfig}
import it.unibo.agar.protocol.PlayerCommand
import it.unibo.agar.view.LocalView

import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.SwingUtilities
import scala.util.{Failure, Success, Try}

object ClientApp:

  def main(args: Array[String]): Unit =
    val playerId = args.find(a => !a.startsWith("-")).getOrElse("p1")
    val host     = args.find(a => a.startsWith("--host=")).map(_.stripPrefix("--host=")).getOrElse("localhost")

    println(s"[ClientApp] Starting Agar.io Client for player '$playerId'...")
    println(s"[ClientApp] Connecting to RabbitMQ at '$host'...")

    // connection to rabbitmq
    val connection = Try(RabbitMQConfig.connect(host)) match
      case Success(conn) =>
        println(s"[ClientApp] Connected to RabbitMQ broker.")
        conn
      case Failure(ex) =>
        System.err.println(s"[ClientApp] Failed to connect to RabbitMQ at '$host': ${ex.getMessage}")
        System.err.println("[ClientApp] Make sure RabbitMQ is running (e.g. 'docker compose up -d').")
        sys.exit(1)

    // start view and network adapter
    var localViewOpt: Option[LocalView] = None

    val networkAdapter = ClientNetworkAdapter(
      connection = connection,
      onWorldSnapshotReceived =
        snapshot => SwingUtilities.invokeLater(() => localViewOpt.foreach(_.updateSnapshot(snapshot)))
    )

    val localView = LocalView(
      playerId = playerId,
      onDirectionChanged = (dx, dy) => networkAdapter.sendCommand(PlayerCommand.Move(playerId, dx, dy))
    )
    localViewOpt = Some(localView)

    var hasShutdown = false
    val shutdown: () => Unit = () =>
      if !hasShutdown then
        hasShutdown = true
        println(s"[ClientApp] Leaving game for player '$playerId'...")
        Try(networkAdapter.sendCommand(PlayerCommand.Leave(playerId)))
        Try(networkAdapter.close())
        Try(connection.close())
        println(s"[ClientApp] Client for player '$playerId' stopped.")

    localView.addWindowListener(
      new WindowAdapter:
        override def windowClosing(e: WindowEvent): Unit =
          shutdown()
          sys.exit(0)
    )

    sys.addShutdownHook(shutdown())

    // start interface and join
    SwingUtilities.invokeLater(() => localView.setVisible(true))
    networkAdapter.sendCommand(PlayerCommand.Join(playerId))

    println(s"[ClientApp] Client for player '$playerId' is running.")
