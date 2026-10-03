package it.unibo.agar.view

import it.unibo.agar.protocol.WorldSnapshot

import java.awt.event.{MouseEvent, MouseMotionAdapter}
import java.awt.{BorderLayout, Dimension, Point}
import javax.swing.{JFrame, WindowConstants}

class LocalView(
  val playerId: String,
  onDirectionChanged: (Double, Double) => Unit = (_, _) => ()
) extends JFrame:

  setTitle(s"Agar.io - Local View ($playerId) (Scala)")
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
  setPreferredSize(Dimension(600, 600))

  private val gamePanel = GamePanel(Option(playerId))
  add(gamePanel, BorderLayout.CENTER)

  setupMouseControls()

  pack()
  setLocationRelativeTo(null)

  def repaintView(): Unit =
    if gamePanel != null then gamePanel.repaint()

  def updateSnapshot(snapshot: WorldSnapshot): Unit =
    gamePanel.updateSnapshot(snapshot)

  private def setupMouseControls(): Unit =
    gamePanel.addMouseMotionListener(
      new MouseMotionAdapter:
        override def mouseMoved(e: MouseEvent): Unit =
          val mousePos: Point = e.getPoint
          val dx              = mousePos.getX - gamePanel.getWidth / 2.0
          val dy              = mousePos.getY - gamePanel.getHeight / 2.0
          val magnitude       = Math.sqrt(dx * dx + dy * dy)
          if magnitude > 0 then
            onDirectionChanged(
              (dx / magnitude) * LocalView.SENSITIVITY,
              (dy / magnitude) * LocalView.SENSITIVITY
            )
          else onDirectionChanged(0, 0)
    )

object LocalView:
  val SENSITIVITY: Double = 2.0
