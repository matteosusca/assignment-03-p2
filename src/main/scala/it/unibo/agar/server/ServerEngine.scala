package it.unibo.agar.server

import it.unibo.agar.model.GameStateManager
import it.unibo.agar.protocol.{PlayerCommand, WorldSnapshot}

class ServerEngine(
  val gameStateManager: GameStateManager,
  broadcastSnapshot: WorldSnapshot => Unit,
  onTickExecuted: () => Unit = () => (),
  val tickIntervalMs: Long = 30L
):

  def enqueueCommand(command: PlayerCommand): Unit = ()

  def step(): Unit = ()

  def start(): Unit = ()

  def stop(): Unit = ()
