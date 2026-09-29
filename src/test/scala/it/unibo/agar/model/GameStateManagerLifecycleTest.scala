package it.unibo.agar.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GameStateManagerLifecycleTest extends AnyFunSuite with Matchers:

  test("join should add a new player to the world"):
    val world = World(0, 0, List.empty, List.empty)
    val manager = DefaultGameStateManager(world)

    manager.join("p1")

    val player = manager.world.getPlayerById("p1")
    player.isDefined shouldBe true
    player.get.id shouldBe "p1"