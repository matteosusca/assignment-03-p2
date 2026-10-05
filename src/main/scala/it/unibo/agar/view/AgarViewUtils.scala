package it.unibo.agar.view

import it.unibo.agar.model.Food
import it.unibo.agar.protocol.WorldSnapshot

import java.awt.{Color, Font, Graphics2D}

object AgarViewUtils:

  val FOOD_RADIUS: Double = massToRadius(Food.DEFAULT_MASS)
  private val PLAYER_BORDER_COLOR: Color = Color.BLACK
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
      // Draw player ID and mass
      g.setColor(PLAYER_BORDER_COLOR)
      val label = s"${player.id} (${player.mass.toInt})"
      val labelWidth = g.getFontMetrics.stringWidth(label)
      val labelX = positioning.x + positioning.radius - (labelWidth / 2)
      val labelY = positioning.y - 5
      g.drawString(label, labelX, labelY)

  def drawGameOver(g: Graphics2D, snapshot: WorldSnapshot, screenWidth: Int, screenHeight: Int): Unit =
    val text = snapshot.winnerId match
      case Some(winner) => s"GAME OVER - WINNER: $winner"
      case None         => "GAME OVER"

    val prevFont = g.getFont
    val font = Font("SansSerif", Font.BOLD, 26)
    g.setFont(font)
    val metrics = g.getFontMetrics(font)
    val textWidth = metrics.stringWidth(text)
    val textHeight = metrics.getHeight

    val bannerHeight = 80
    val bannerY = (screenHeight - bannerHeight) / 2

    // Dark semi-transparent background banner across the screen
    g.setColor(Color(0, 0, 0, 190))
    g.fillRect(0, bannerY, screenWidth, bannerHeight)

    // Golden / Yellow text centered in the banner
    g.setColor(Color.YELLOW)
    val textX = (screenWidth - textWidth) / 2
    val textY = bannerY + ((bannerHeight - textHeight) / 2) + metrics.getAscent
    g.drawString(text, textX, textY)

    g.setFont(prevFont)

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
