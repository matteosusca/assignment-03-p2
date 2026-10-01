package it.unibo.agar.server

import it.unibo.agar.model.GameStateManager
import it.unibo.agar.protocol.{PlayerCommand, WorldSnapshot}

import java.util.concurrent.{ConcurrentLinkedQueue, Executors, ScheduledExecutorService, TimeUnit}

class ServerEngine(
  val gameStateManager: GameStateManager,
  broadcastSnapshot: WorldSnapshot => Unit,
  onTickExecuted: () => Unit = () => (),
  val tickIntervalMs: Long = 30L
) extends AutoCloseable:

  private val commandQueue: ConcurrentLinkedQueue[PlayerCommand] = ConcurrentLinkedQueue()
  private var scheduler: Option[ScheduledExecutorService]        = None

  def enqueueCommand(command: PlayerCommand): Unit =
    commandQueue.offer(command)

  def step(): Unit =
    processPendingCommands()
    gameStateManager.tick()
    val snapshot = gameStateManager.toSnapshot()
    broadcastSnapshot(snapshot)
    onTickExecuted()

  private def processPendingCommands(): Unit =
    var command = commandQueue.poll()
    while command != null do
      command match
        case PlayerCommand.Join(id)         => gameStateManager.join(id)
        case PlayerCommand.Move(id, dx, dy) => gameStateManager.setPlayerDirection(id, dx, dy)
        case PlayerCommand.Leave(id)        => gameStateManager.leave(id)
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
