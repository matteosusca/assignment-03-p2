package it.unibo.agar

import it.unibo.agar.model.*
import it.unibo.agar.view.{GlobalView, LocalView}
import javax.swing.SwingUtilities
import java.util.{Timer, TimerTask}
import scala.collection.mutable.ListBuffer

object Main:
  private val WORLD_WIDTH = 1000
  private val WORLD_HEIGHT = 1000
  private val NUM_PLAYERS = 4 // p1, p2, p3, p4
  private val NUM_FOODS = 100
  private val GAME_TICK_MS = 30L // Corresponds to ~33 FPS

  @FunctionalInterface
  trait JFrameRepaintable:
    def repaintView(): Unit

  def main(args: Array[String]): Unit =
    val initialPlayers = GameInitializer.initialPlayers(NUM_PLAYERS, WORLD_WIDTH, WORLD_HEIGHT)
    val initialFoods = GameInitializer.initialFoods(NUM_FOODS, WORLD_WIDTH, WORLD_HEIGHT)
    val initialWorld = World(WORLD_WIDTH, WORLD_HEIGHT, initialPlayers, initialFoods)
    val gameManager: GameStateManager = DefaultGameStateManager(initialWorld)

    val views = ListBuffer[JFrameRepaintable]()

    SwingUtilities.invokeLater(() =>
      val globalView = GlobalView(gameManager)
      views += (() => globalView.repaintView())
      globalView.setVisible(true)

      val localViewP1 = LocalView(gameManager, "p1")
      views += (() => localViewP1.repaintView())
      localViewP1.setVisible(true)

      val localViewP2 = LocalView(gameManager, "p2")
      views += (() => localViewP2.repaintView())
      localViewP2.setVisible(true)
    )

    val timer = Timer(true)
    timer.scheduleAtFixedRate(new TimerTask:
      override def run(): Unit =
        AIMovement.moveAI("p1", gameManager)
        AIMovement.moveAI("p3", gameManager)
        AIMovement.moveAI("p4", gameManager)

        gameManager.tick()

        SwingUtilities.invokeLater(() =>
          views.foreach(_.repaintView())
        )
    , 0, GAME_TICK_MS)
