package it.unibo.agar.model

trait GameStateManager:
  def world: World
  def getWorld: World = world
  def setPlayerDirection(playerId: String, dx: Double, dy: Double): Unit
  def tick(): Unit
