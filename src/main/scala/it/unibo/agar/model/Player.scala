package it.unibo.agar.model

case class Player(
    override val id: String,
    override val x: Double,
    override val y: Double,
    override val mass: Double
) extends AbstractEntity(id, x, y, mass):

  def grow(entity: Entity): Player =
    Player(id, x, y, mass + entity.mass)

  def moveTo(newX: Double, newY: Double): Player =
    Player(id, newX, newY, mass)
