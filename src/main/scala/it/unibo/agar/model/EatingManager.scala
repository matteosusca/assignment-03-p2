package it.unibo.agar.model

object EatingManager:
  private val MASS_MARGIN: Double = 1.1 // 10% bigger to eat

  private def collides(e1: Entity, e2: Entity): Boolean =
    e1.distanceTo(e2) < (e1.radius + e2.radius)

  def canEatFood(player: Player, food: Food): Boolean =
    collides(player, food) && player.mass > food.mass

  def canEatPlayer(player: Player, other: Player): Boolean =
    collides(player, other) && player.mass > other.mass * MASS_MARGIN
