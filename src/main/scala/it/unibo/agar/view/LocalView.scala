package it.unibo.agar.view

import it.unibo.agar.model.GameStateManager
import javax.swing.{JFrame, WindowConstants}
import java.awt.{BorderLayout, Dimension, Point}
import java.awt.event.{MouseEvent, MouseMotionAdapter}

class LocalView(val gameStateManager: GameStateManager, val playerId: String) extends JFrame:
  setTitle(s"Agar.io - Local View ($playerId) (Scala)")
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  setPreferredSize(Dimension(600, 600))

  private val gamePanel = GamePanel(gameStateManager, playerId)
  add(gamePanel, BorderLayout.CENTER)

  setupMouseControls()

  pack()
  setLocationRelativeTo(null)

  private def setupMouseControls(): Unit =
    gamePanel.addMouseMotionListener(
      new MouseMotionAdapter:
        override def mouseMoved(e: MouseEvent): Unit =
          gameStateManager.world.getPlayerById(playerId).foreach { _ =>
            val mousePos: Point = e.getPoint
            val viewCenterX     = gamePanel.getWidth / 2.0
            val viewCenterY     = gamePanel.getHeight / 2.0

            val dx = mousePos.x - viewCenterX
            val dy = mousePos.y - viewCenterY

            val magnitude = Math.hypot(dx, dy)
            if magnitude > 0 then
              gameStateManager.setPlayerDirection(
                playerId,
                (dx / magnitude) * LocalView.SENSITIVITY,
                (dy / magnitude) * LocalView.SENSITIVITY
              )
            else gameStateManager.setPlayerDirection(playerId, 0, 0)
          }
    )

  def repaintView(): Unit =
    if gamePanel != null then gamePanel.repaint()

object LocalView:
  val SENSITIVITY: Double = 2.0
