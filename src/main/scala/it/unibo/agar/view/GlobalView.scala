package it.unibo.agar.view

import it.unibo.agar.protocol.WorldSnapshot

import java.awt.{BorderLayout, Dimension}
import javax.swing.{JFrame, WindowConstants}

class GlobalView extends JFrame:
  setTitle("Agar.io - Global View (Scala)")
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  setPreferredSize(Dimension(800, 800))

  private val gamePanel = GamePanel(None)
  add(gamePanel, BorderLayout.CENTER)

  pack()
  setLocationRelativeTo(null)

  def updateSnapshot(snapshot: WorldSnapshot): Unit =
    gamePanel.updateSnapshot(snapshot)
