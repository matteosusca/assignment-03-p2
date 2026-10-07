package it.unibo.agar.view

import it.unibo.agar.protocol.WorldSnapshot

import java.awt.{Color, Graphics, Graphics2D, RenderingHints, Toolkit}
import javax.swing.JPanel

class GamePanel(val focusedPlayerId: Option[String] = None) extends JPanel:

  private var currentSnapshot: Option[WorldSnapshot] = None

  def this(focusedPlayerId: String) =
    this(Option(focusedPlayerId))

  setFocusable(true)
  setDoubleBuffered(true)
  setBackground(Color.WHITE)

  def updateSnapshot(snapshot: WorldSnapshot): Unit =
    currentSnapshot = Some(snapshot)
    repaint()

  override protected def paintComponent(g: Graphics): Unit =
    super.paintComponent(g)
    val g2d = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)

    currentSnapshot.foreach { snapshot =>
      val (offsetX, offsetY) = focusedPlayerId.flatMap(pid => snapshot.players.find(_.id == pid)) match
        case Some(player) => (player.x - getWidth / 2.0, player.y - getHeight / 2.0)
        case None         => (0.0, 0.0)
      AgarViewUtils.drawWorld(g2d, snapshot, offsetX, offsetY)
      if snapshot.isGameOver then
        AgarViewUtils.drawGameOver(g2d, snapshot, getWidth, getHeight)
    }

    Toolkit.getDefaultToolkit.sync()