package it.unibo.agar.model

case class Food(
  override val id: String,
  override val x: Double,
  override val y: Double,
  override val mass: Double
) extends AbstractEntity(id, x, y, mass)

object Food:
  val DEFAULT_MASS: Double = 100.0
