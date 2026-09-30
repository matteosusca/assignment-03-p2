package it.unibo.agar.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GameStateManagerFoodTest extends AnyFunSuite with Matchers:

  private val WORLD_WIDTH  = 1000
  private val WORLD_HEIGHT = 1000

  private def createManager(
    players: List[Player] = List.empty,
    foods: List[Food] = List.empty
  ): GameStateManager =
    val world = World(WORLD_WIDTH, WORLD_HEIGHT, players, foods)
    DefaultGameStateManager(world)

  test("tick should refill foods up to MAX_FOOD_ITEMS when world has no food"):
    val manager = createManager(foods = List.empty)
    manager.world.foods shouldBe empty

    manager.tick()

    manager.world.foods.size shouldBe DefaultGameStateManager.MAX_FOOD_ITEMS

  test("tick should refill missing foods up to MAX_FOOD_ITEMS when world has partial food"):
    val initialFoods = (1 to 50).map(i => Food(s"initial_f$i", 10.0, 10.0, Food.DEFAULT_MASS)).toList
    val manager      = createManager(foods = initialFoods)
    manager.world.foods.size shouldBe 50

    manager.tick()

    manager.world.foods.size shouldBe DefaultGameStateManager.MAX_FOOD_ITEMS

  test("tick should keep food count at MAX_FOOD_ITEMS after a player eats food"):
    val foodToEat = Food("target_food", 100.0, 100.0, Food.DEFAULT_MASS)
    val otherFoods = (1 until DefaultGameStateManager.MAX_FOOD_ITEMS).map(i =>
      Food(s"f$i", 500.0, 500.0, Food.DEFAULT_MASS)
    ).toList
    val player  = Player("p1", 100.0, 100.0, 120.0) // Positioned directly over foodToEat
    val manager = createManager(players = List(player), foods = foodToEat :: otherFoods)

    manager.world.foods.size shouldBe DefaultGameStateManager.MAX_FOOD_ITEMS

    manager.tick()

    val updatedPlayer = manager.world.getPlayerById("p1").get
    updatedPlayer.mass should be > 120.0
    manager.world.foods.size shouldBe DefaultGameStateManager.MAX_FOOD_ITEMS

  test("tick should not add foods if world already has MAX_FOOD_ITEMS"):
    val initialFoods = (1 to DefaultGameStateManager.MAX_FOOD_ITEMS).map(i =>
      Food(s"f$i", 500.0, 500.0, Food.DEFAULT_MASS)
    ).toList
    val manager = createManager(foods = initialFoods)

    manager.tick()

    manager.world.foods.size shouldBe DefaultGameStateManager.MAX_FOOD_ITEMS

  test("all refilled foods should be placed within world boundaries and have unique ids"):
    val manager = createManager(foods = List.empty)

    manager.tick()

    val foods = manager.world.foods
    foods.size shouldBe DefaultGameStateManager.MAX_FOOD_ITEMS

    for food <- foods do
      food.x should be >= 0.0
      food.x should be <= WORLD_WIDTH.toDouble
      food.y should be >= 0.0
      food.y should be <= WORLD_HEIGHT.toDouble

    foods.map(_.id).toSet.size shouldBe foods.size
