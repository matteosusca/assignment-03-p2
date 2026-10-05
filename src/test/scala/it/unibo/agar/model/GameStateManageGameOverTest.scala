package it.unibo.agar.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GameStateManageGameOverTest extends AnyFunSuite with Matchers:

  test("toSnapshot should report isGameOver=false and winnerId=None when all the players are below WINNING_MASS"):
    val player = Player("p1", 100.0, 100.0, mass = 200)
    val world = World(1000, 1000, List(player), List.empty)
    val manager = DefaultGameStateManager(world, FoodRefillStrategy.immediate(0))

    val snapshot = manager.toSnapshot()
    snapshot.isGameOver shouldBe false
    snapshot.winnerId shouldBe None

  test("toSnapshot should report isGameOver=true and winnerId=playerId when a player reaches WINNING_MASS"):
    val p1 = Player("p1", 100.0, 100.0, mass = DefaultGameStateManager.WINNING_MASS)
    val p2 = Player("p2", 200.0, 200.0, mass = 100)
    val world = World(1000, 1000, List(p1, p2), List.empty)
    val manager = DefaultGameStateManager(world, FoodRefillStrategy.immediate(0))

    val snapshot = manager.toSnapshot()
    snapshot.isGameOver shouldBe true
    snapshot.winnerId shouldBe Some("p1")

  test("tick should freeze simulation and not increase tickNumber once the game is over"):
    val p1 = Player("p1", 100.0, 100.0, mass = DefaultGameStateManager.WINNING_MASS)
    val world = World(1000, 1000, List(p1), List.empty)
    val manager = DefaultGameStateManager(world, FoodRefillStrategy.immediate(0))

    manager.setPlayerDirection("p1", 1.0, 0.0) // Attempt to move the player
    manager.tick() // This tick should not change the state since the game is over

    val snapshot = manager.toSnapshot()
    snapshot.isGameOver shouldBe true
    snapshot.winnerId shouldBe Some("p1")
    snapshot.tickNumber shouldBe 0 // tickNumber should not have increased
    snapshot.players.head.x shouldBe 100.0 // Player position should not have changed