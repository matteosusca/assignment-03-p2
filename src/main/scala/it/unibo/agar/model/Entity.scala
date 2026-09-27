package it.unibo.agar.model

trait Entity:
  def id: String
  def mass: Double
  def x: Double
  def y: Double
  def radius: Double

  def getId: String = id
  def getMass: Double = mass
  def getX: Double = x
  def getY: Double = y
  def getRadius: Double = radius

  def distanceTo(other: Entity): Double =
    val dx = x - other.x
    val dy = y - other.y
    Math.hypot(dx, dy)
