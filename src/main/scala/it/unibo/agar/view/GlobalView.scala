package it.unibo.agar.view

import it.unibo.agar.model.GameStateManager
import javax.swing.{JFrame, WindowConstants}
import java.awt.{BorderLayout, Dimension}

class GlobalView(gameStateManager: GameStateManager) extends JFrame:
  setTitle("Agar.io - Global View (Scala)")
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  setPreferredSize(Dimension(800, 800))

  private val gamePanel = GamePanel(gameStateManager)
  add(gamePanel, BorderLayout.CENTER)

  pack()
  setLocationRelativeTo(null)

  def repaintView(): Unit =
    if gamePanel != null then gamePanel.repaint()
