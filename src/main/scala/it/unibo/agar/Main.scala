package it.unibo.agar

import it.unibo.agar.model.*
import it.unibo.agar.protocol.WorldSnapshot
import it.unibo.agar.view.{GlobalView, LocalView}

import java.util.{Timer, TimerTask}
import javax.swing.SwingUtilities
import scala.collection.mutable.ListBuffer

object Main:
  private val WORLD_WIDTH  = 1000
  private val WORLD_HEIGHT = 1000
  private val NUM_PLAYERS  = 4
  private val NUM_FOODS    = 100
  private val GAME_TICK_MS = 30L

  def main(args: Array[String]): Unit =
    val initialPlayers                = GameInitializer.initialPlayers(NUM_PLAYERS, WORLD_WIDTH, WORLD_HEIGHT)
    val initialFoods                  = GameInitializer.initialFoods(NUM_FOODS, WORLD_WIDTH, WORLD_HEIGHT)
    val initialWorld                  = World(WORLD_WIDTH, WORLD_HEIGHT, initialPlayers, initialFoods)
    val gameManager: GameStateManager = DefaultGameStateManager(initialWorld)

    val viewUpdaters = ListBuffer[WorldSnapshot => Unit]()

    SwingUtilities.invokeLater(() =>
      val globalView  = GlobalView()
      val localViewP1 = LocalView("p1", (dx, dy) => gameManager.setPlayerDirection("p1", dx, dy))
      val localViewP2 = LocalView("p2", (dx, dy) => gameManager.setPlayerDirection("p2", dx, dy))

      List(globalView, localViewP1, localViewP2).foreach(_.setVisible(true))
      viewUpdaters ++= List(globalView.updateSnapshot, localViewP1.updateSnapshot, localViewP2.updateSnapshot)
    )

    Timer(true).scheduleAtFixedRate(
      new TimerTask:
        override def run(): Unit =
          List("p1", "p3", "p4").foreach(AIMovement.moveAI(_, gameManager))
          gameManager.tick()

          val snapshot = gameManager.toSnapshot()
          SwingUtilities.invokeLater(() => viewUpdaters.foreach(_(snapshot)))
      ,
      0L,
      GAME_TICK_MS
    )
