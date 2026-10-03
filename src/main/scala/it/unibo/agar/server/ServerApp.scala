package it.unibo.agar.server

import it.unibo.agar.model.*
import it.unibo.agar.network.{RabbitMQConfig, ServerNetworkAdapter}
import it.unibo.agar.view.GlobalView

import javax.swing.SwingUtilities
import scala.util.{Failure, Success, Try}

object ServerApp:
  private val WORLD_WIDTH   = 1000
  private val WORLD_HEIGHT  = 1000
  private val INITIAL_FOODS = 100

  def main(args: Array[String]): Unit =
    val showGui = args.contains("--gui") || args.contains("-g")
    val host = args
      .find(a => !a.startsWith("-"))
      .orElse(args.find(_.startsWith("--host=")).map(_.stripPrefix("--host=")))
      .getOrElse("localhost")

    println(s"[ServerApp] Initializing Agar.io Authoritative Server...")
    println(s"[ServerApp] Connecting to RabbitMQ at '$host'...")

    val connection = Try(RabbitMQConfig.connect(host)) match
      case Success(conn) =>
        println(s"[ServerApp] Connected to RabbitMQ broker.")
        conn
      case Failure(ex) =>
        System.err.println(s"[ServerApp] Failed to connect to RabbitMQ at '$host': ${ex.getMessage}")
        System.err.println("[ServerApp] Make sure RabbitMQ is running (e.g. 'docker compose up -d').")
        sys.exit(1)

    // Initial world with food items and zero players (players will join dynamically via RabbitMQ)
    val initialFoods     = GameInitializer.initialFoods(INITIAL_FOODS, WORLD_WIDTH, WORLD_HEIGHT)
    val initialWorld     = World(WORLD_WIDTH, WORLD_HEIGHT, List.empty, initialFoods)
    val gameStateManager = DefaultGameStateManager(initialWorld)

    var globalViewOpt: Option[GlobalView] = None
    if showGui then
      SwingUtilities.invokeLater(() =>
        val view = GlobalView()
        view.setVisible(true)
        globalViewOpt = Some(view)
      )

    var networkAdapter: Option[ServerNetworkAdapter] = None

    val engine = ServerEngine(
      gameStateManager = gameStateManager,
      broadcastSnapshot = snapshot =>
        networkAdapter.foreach(_.broadcastWorldSnapshot(snapshot))
        if showGui then SwingUtilities.invokeLater(() => globalViewOpt.foreach(_.updateSnapshot(snapshot)))
    )

    val adapter = ServerNetworkAdapter(connection, engine.enqueueCommand)
    networkAdapter = Some(adapter)

    sys.addShutdownHook:
      println("\n[ServerApp] Shutdown signal received. Stopping server...")
      engine.stop()
      networkAdapter.foreach(_.close())
      connection.close()
      println("[ServerApp] Server stopped cleanly.")

    engine.start()

    println(s"[ServerApp] Server started successfully!")
    println(s"[ServerApp] World dimensions: ${WORLD_WIDTH}x$WORLD_HEIGHT, initial food: ${initialFoods.size}")
    println(s"[ServerApp] Listening for commands on queue: '${RabbitMQConfig.CommandsQueue}'")
    println(s"[ServerApp] Broadcasting world snapshots on exchange: '${RabbitMQConfig.WorldExchange}'")
    if showGui then println("[ServerApp] GlobalView debug GUI is enabled.")
    println("[ServerApp] Press Ctrl+C to terminate.")
