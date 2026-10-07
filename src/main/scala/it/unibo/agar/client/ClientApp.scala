package it.unibo.agar.client

import it.unibo.agar.network.{ClientNetworkAdapter, RabbitMQConfig}
import it.unibo.agar.protocol.PlayerCommand
import it.unibo.agar.protocol.WorldSnapshot
import it.unibo.agar.view.LocalView
import java.awt.event.{WindowAdapter, WindowEvent}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.concurrent.{Executors, TimeUnit}
import javax.swing.SwingUtilities
import scala.util.{Failure, Success, Try}

object ClientApp:

  val MOVE_SAMPLING_INTERVAL_MS: Long = 30L

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
    var gameOverLogged: Boolean         = false

    val latestSnapshot   = new AtomicReference[WorldSnapshot]()
    val isRepaintPending = new AtomicBoolean(false)

    val networkAdapter = ClientNetworkAdapter(
      connection = connection,
      onWorldSnapshotReceived = snapshot =>
        if snapshot.isGameOver && !gameOverLogged then
          gameOverLogged = true
          val winnerMsg = snapshot.winnerId.map(w => s"Winner is '$w'!").getOrElse("Game Over!")
          println(s"[ClientApp] Game Over reached! $winnerMsg")
        latestSnapshot.set(snapshot)
        if isRepaintPending.compareAndSet(false, true) then
          SwingUtilities.invokeLater: () =>
            isRepaintPending.set(false)
            val current = latestSnapshot.get()
            if current != null then localViewOpt.foreach(_.updateSnapshot(current))
    )

    val currentDirection  = new AtomicReference[(Double, Double)]((0.0, 0.0))
    var lastSentDirection = (0.0, 0.0)

    val localView = LocalView(
      playerId = playerId,
      onDirectionChanged = (dx, dy) => currentDirection.set((dx, dy))
    )
    localViewOpt = Some(localView)

    val clientScheduler = Executors.newSingleThreadScheduledExecutor(r =>
      val t = Thread(r, s"client-io-$playerId")
      t.setDaemon(true)
      t
    )

    clientScheduler.scheduleAtFixedRate(
      () =>
        val (dx, dy) = currentDirection.get()
        if (dx != 0.0 || dy != 0.0) || (lastSentDirection._1 != 0.0 || lastSentDirection._2 != 0.0) then
          lastSentDirection = (dx, dy)
          Try(networkAdapter.sendCommand(PlayerCommand.Move(playerId, dx, dy))),
      0L,
      ClientApp.MOVE_SAMPLING_INTERVAL_MS,
      TimeUnit.MILLISECONDS
    )

    clientScheduler.scheduleAtFixedRate(
      () => Try(networkAdapter.sendCommand(PlayerCommand.Heartbeat(playerId))),
      2L,
      2L,
      TimeUnit.SECONDS
    )

    var hasShutdown = false
    val shutdown: () => Unit = () =>
      if !hasShutdown then
        hasShutdown = true
        clientScheduler.shutdown()
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
