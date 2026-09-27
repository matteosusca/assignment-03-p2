package it.unibo.agar.model

abstract class AbstractEntity(
    val id: String,
    val x: Double,
    val y: Double,
    val mass: Double
) extends Entity:
  val radius: Double = Math.sqrt(mass / Math.PI)
