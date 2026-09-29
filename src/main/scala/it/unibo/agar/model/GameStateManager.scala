package it.unibo.agar.model

import it.unibo.agar.protocol.WorldSnapshot

trait GameStateManager:
  def world: World
  def getWorld: World = world
  def setPlayerDirection(playerId: String, dx: Double, dy: Double): Unit
  def tick(): Unit
  def join(playerId: String): Unit
  def join(playerId: String, pos: Position, mass: Double = Player.DEFAULT_MASS): Unit
  def leave(playerId: String): Unit
  def toSnapshot(): WorldSnapshot