package it.unibo.agar.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GameStateManagerLifecycleTest extends AnyFunSuite with Matchers:

  private def createManager(): GameStateManager =
    val world = World(0, 0, List.empty, List.empty)
    DefaultGameStateManager(world)

  test("join should add a new player to the world"):
    val manager = createManager()

    manager.join("p1")

    val player = manager.world.getPlayerById("p1")
    player.isDefined shouldBe true
    player.get.id shouldBe "p1"

  test("join should be idempotent for the same playerId"):
    val manager = createManager()

    manager.join("p1")
    manager.join("p1") // Attempt to join the same player again

    manager.world.players.size shouldBe 1

  test("leave should remove a player from the world"):
    val manager = createManager()

    manager.join("p1")
    manager.join("p2")
    manager.world.players.size shouldBe 2

    manager.leave("p1")
    manager.world.players.size shouldBe 1
    manager.world.getPlayerById("p1") shouldBe None
    manager.world.getPlayerById("p2").isDefined shouldBe true