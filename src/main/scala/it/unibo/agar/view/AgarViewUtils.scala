package it.unibo.agar.view

import it.unibo.agar.model.{Entity, Food, Player, World}
import java.awt.{Color, Graphics2D}

object AgarViewUtils:

  private val PLAYER_BORDER_COLOR: Color = Color.BLACK
  private val PLAYER_LABEL_OFFSET_X: Int = 10
  private val PLAYER_LABEL_OFFSET_Y: Int = 0
  private val PLAYER_PALETTE: Array[Color] = Array(
    Color.BLUE, Color.ORANGE, Color.CYAN, Color.PINK,
    Color.YELLOW, Color.RED, Color.GREEN, Color.LIGHT_GRAY
  )

  private def getPlayerColor(id: String): Color =
    if id != null && id.startsWith("p") then
      try
        val index = id.substring(1).toInt
        PLAYER_PALETTE(Math.abs(index - 1) % PLAYER_PALETTE.length)
      catch
        case _: NumberFormatException => Color.GRAY
    else
      Color.GRAY

  private case class PositioningInfo(x: Int, y: Int, radius: Int):
    def diameter: Int = radius * 2

  private def getPositioningInfo(entity: Entity, offsetX: Double, offsetY: Double): PositioningInfo =
    val radius = entity.radius.toInt
    val x = (entity.x - offsetX - radius).toInt
    val y = (entity.y - offsetY - radius).toInt
    PositioningInfo(x, y, radius)

  def drawWorld(g: Graphics2D, world: World, offsetX: Double, offsetY: Double): Unit =
    // Draw foods
    g.setColor(Color.GREEN)
    for food <- world.foods do
      val positioning = getPositioningInfo(food, offsetX, offsetY)
      g.fillOval(positioning.x, positioning.y, positioning.diameter, positioning.diameter)

    // Draw players
    for player <- world.players do
      val positioning = getPositioningInfo(player, offsetX, offsetY)
      g.setColor(getPlayerColor(player.id))
      g.fillOval(positioning.x, positioning.y, positioning.diameter, positioning.diameter)
      // Draw player ID
      g.setColor(PLAYER_BORDER_COLOR)
      // Adjust label position to be relative to the player's actual center on screen
      val labelX = positioning.x - PLAYER_LABEL_OFFSET_X
      val labelY = positioning.y - PLAYER_LABEL_OFFSET_Y
      g.drawString(player.id, labelX, labelY)
