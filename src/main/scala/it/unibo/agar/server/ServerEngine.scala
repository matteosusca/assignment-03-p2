package it.unibo.agar.server

import it.unibo.agar.model.GameStateManager
import it.unibo.agar.protocol.{PlayerCommand, WorldSnapshot}

import java.util.concurrent.{ConcurrentLinkedQueue, Executors, ScheduledExecutorService, TimeUnit}

class ServerEngine(
  val gameStateManager: GameStateManager,
  broadcastSnapshot: WorldSnapshot => Unit,
  onTickExecuted: () => Unit = () => (),
  val tickIntervalMs: Long = 30L,
  val inactivityThresholdMs: Long = ServerEngine.DEFAULT_INACTIVE_TIMEOUT_MS,
  val timeProvider: () => Long = () => System.currentTimeMillis()
) extends AutoCloseable:

  private val commandQueue: ConcurrentLinkedQueue[PlayerCommand] = ConcurrentLinkedQueue()
  private var scheduler: Option[ScheduledExecutorService]        = None
  private val lastActivityTimestamps: collection.mutable.Map[String, Long] = collection.mutable.Map()
  
  def enqueueCommand(command: PlayerCommand): Unit =
    commandQueue.offer(command)

  def step(): Unit =
    processPendingCommands()
    evictInactivePlayers()
    gameStateManager.tick()
    val snapshot = gameStateManager.toSnapshot()
    broadcastSnapshot(snapshot)
    onTickExecuted()

  private def evictInactivePlayers(): Unit =
    val now = timeProvider()
    val (active, expired) = lastActivityTimestamps.partition:
      (_, lastSeen) => now - lastSeen <= inactivityThresholdMs
    if expired.nonEmpty then
      expired.keys.foreach: id =>
        gameStateManager.leave(id)
        println(s"[ServerEngine] Evicting inactive player: $id")
      lastActivityTimestamps --= expired.keys

  private def processPendingCommands(): Unit =
    val now = timeProvider()
    var command = commandQueue.poll()
    while command != null do
      command match
        case PlayerCommand.Join(id) =>
          gameStateManager.join(id)
          lastActivityTimestamps(id) = now
        case PlayerCommand.Move(id, dx, dy) =>
          gameStateManager.setPlayerDirection(id, dx, dy)
          if gameStateManager.world.getPlayerById(id).isDefined then lastActivityTimestamps(id) = now
        case PlayerCommand.Heartbeat(id) =>
          if gameStateManager.world.getPlayerById(id).isDefined then lastActivityTimestamps(id) = now
        case PlayerCommand.Leave(id) =>
          gameStateManager.leave(id)
          lastActivityTimestamps.remove(id)
      command = commandQueue.poll()

  def start(): Unit =
    if scheduler.isEmpty then
      val executor = Executors.newSingleThreadScheduledExecutor()
      executor.scheduleAtFixedRate(
        () =>
          try step()
          catch
            case ex: Throwable =>
              ex.printStackTrace(),
        0L,
        tickIntervalMs,
        TimeUnit.MILLISECONDS
      )
      scheduler = Some(executor)

  def stop(): Unit =
    scheduler.foreach { exec =>
      exec.shutdown()
      exec.awaitTermination(1, TimeUnit.SECONDS)
    }
    scheduler = None

  override def close(): Unit = stop()

object ServerEngine:
  val DEFAULT_INACTIVE_TIMEOUT_MS: Long = 30_000L
