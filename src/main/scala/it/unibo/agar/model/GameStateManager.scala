package it.unibo.agar.model

trait GameStateManager:
  def world: World
  def getWorld: World = world
  def setPlayerDirection(playerId: String, dx: Double, dy: Double): Unit
  def tick(): Unit
  def join(playerId: String): Unit
  def join(playerId: String, x: Double, y: Double, mass: Double = Player.DEFAULT_MASS): Unit
