package it.unibo.agar.view

import it.unibo.agar.model.Food
import it.unibo.agar.protocol.WorldSnapshot

import java.awt.{Color, Graphics2D}

object AgarViewUtils:

  val FOOD_RADIUS: Double = massToRadius(Food.DEFAULT_MASS)
  private val PLAYER_BORDER_COLOR: Color = Color.BLACK
  private val PLAYER_LABEL_OFFSET_X: Int = 10
  private val PLAYER_LABEL_OFFSET_Y: Int = 0
  private val PLAYER_PALETTE: Array[Color] = Array(
    Color.BLUE,
    Color.ORANGE,
    Color.CYAN,
    Color.PINK,
    Color.YELLOW,
    Color.RED,
    Color.GREEN,
    Color.LIGHT_GRAY
  )

  def drawWorld(g: Graphics2D, snapshot: WorldSnapshot, offsetX: Double, offsetY: Double): Unit =
    // Draw foods
    g.setColor(Color.GREEN)
    for food <- snapshot.foods do
      val positioning = getPositioningInfo(food.x, food.y, FOOD_RADIUS, offsetX, offsetY)
      g.fillOval(positioning.x, positioning.y, positioning.diameter, positioning.diameter)

    // Draw players
    for player <- snapshot.players do
      val positioning = getPositioningInfo(player.x, player.y, massToRadius(player.mass), offsetX, offsetY)
      g.setColor(getPlayerColor(player.id))
      g.fillOval(positioning.x, positioning.y, positioning.diameter, positioning.diameter)
      // Draw player ID
      g.setColor(PLAYER_BORDER_COLOR)
      // Adjust label position to be relative to the player's actual center on screen
      g.drawString(player.id, positioning.x - PLAYER_LABEL_OFFSET_X, positioning.y - PLAYER_LABEL_OFFSET_Y)

  def massToRadius(mass: Double): Double = Math.sqrt(mass / Math.PI)

  private def getPlayerColor(id: String): Color =
    if id != null && id.startsWith("p") then
      try
        val index = id.substring(1).toInt
        PLAYER_PALETTE(Math.abs(index - 1) % PLAYER_PALETTE.length)
      catch case _: NumberFormatException => Color.GRAY
    else Color.GRAY

  private def getPositioningInfo(
    entityX: Double,
    entityY: Double,
    entityRadius: Double,
    offsetX: Double,
    offsetY: Double
  ): PositioningInfo =
    val radius = entityRadius.toInt
    val x      = (entityX - offsetX - radius).toInt
    val y      = (entityY - offsetY - radius).toInt
    PositioningInfo(x, y, radius)

  private case class PositioningInfo(x: Int, y: Int, radius: Int):
    def diameter: Int = radius * 2
