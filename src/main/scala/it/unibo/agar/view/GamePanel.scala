package it.unibo.agar.view

import it.unibo.agar.model.GameStateManager
import javax.swing.JPanel
import java.awt.{Graphics, Graphics2D}

class GamePanel(val gameStateManager: GameStateManager, val focusedPlayerId: Option[String]) extends JPanel:

  def this(gameStateManager: GameStateManager, focusedPlayerId: String) =
    this(gameStateManager, Option(focusedPlayerId))

  def this(gameStateManager: GameStateManager) =
    this(gameStateManager, None)

  setFocusable(true)

  override protected def paintComponent(g: Graphics): Unit =
    super.paintComponent(g)
    val g2d   = g.asInstanceOf[Graphics2D]
    val world = gameStateManager.world

    focusedPlayerId match
      case Some(pid) =>
        world.getPlayerById(pid).foreach { player =>
          val offsetX = player.x - getWidth / 2.0
          val offsetY = player.y - getHeight / 2.0
          AgarViewUtils.drawWorld(g2d, world, offsetX, offsetY)
        }
      case None =>
        AgarViewUtils.drawWorld(g2d, world, 0, 0)
