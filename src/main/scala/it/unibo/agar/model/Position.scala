package it.unibo.agar.model

case class Position(x: Double, y: Double)

object Position:
  val ZERO: Position = Position(0.0, 0.0)

  def of(x: Double, y: Double): Position = Position(x, y)
