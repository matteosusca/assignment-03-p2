package it.unibo.agar.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GameStateManagerMovementTest extends AnyFunSuite with Matchers:

  private val WORLD_WIDTH  = 1000
  private val WORLD_HEIGHT = 1000

  private def createManagerWithPlayer(pos: Position): (DefaultGameStateManager, String) =
    val p = Player("p1", pos.x, pos.y, Player.DEFAULT_MASS)
    val world = World(WORLD_WIDTH, WORLD_HEIGHT, List(p), List.empty)
    val manager = DefaultGameStateManager(world, FoodRefillStrategy.immediate(0))
    (manager, p.id)

  test("player moving within boundaries should update position normally"):
    val (manager, pid) = createManagerWithPlayer(Position(500, 500))
    manager.setPlayerDirection(pid, 1, 0) // move right
    manager.tick()
    val player = manager.world.getPlayerById(pid).get
    player.x shouldBe (500.0 + DefaultGameStateManager.PLAYER_SPEED)
    player.y shouldBe 500.0

  test("player moving past left boundary should be clamped to 0"):
    val (manager, pid) = createManagerWithPlayer(Position(1, 500))
    manager.setPlayerDirection(pid, -1, 0) // move left
    manager.tick()
    val player = manager.world.getPlayerById(pid).get
    player.x shouldBe 0.0

  test("player moving past top boundary should be clamped to 0"):
    val (manager, pid) = createManagerWithPlayer(Position(500, 1))
    manager.setPlayerDirection(pid, 0, -1) // move up
    manager.tick()
    val player = manager.world.getPlayerById(pid).get
    player.y shouldBe 0.0

  test("player moving past right boundary should be clamped to world width"):
    val (manager, pid) = createManagerWithPlayer(Position(WORLD_WIDTH - 1, 500))
    manager.setPlayerDirection(pid, 1, 0) // move right
    manager.tick()
    val player = manager.world.getPlayerById(pid).get
    player.x shouldBe WORLD_WIDTH.toDouble

  test("player moving past bottom boundary should be clamped to world height"):
    val (manager, pid) = createManagerWithPlayer(Position(500, WORLD_HEIGHT - 1))
    manager.setPlayerDirection(pid, 0, 1) // move down
    manager.tick()
    val player = manager.world.getPlayerById(pid).get
    player.y shouldBe WORLD_HEIGHT.toDouble