package it.unibo.agar.model

import java.util.UUID

trait FoodRefillStrategy:
  def refill(world: World): World

object FoodRefillStrategy:
  val DEFAULT_MAX_FOODS: Int     = 150
  val DEFAULT_RATE_PER_TICK: Int = 1

  def default: FoodRefillStrategy = gradual()

  def immediate(maxFoods: Int = DEFAULT_MAX_FOODS): FoodRefillStrategy =
    ImmediateFoodRefillStrategy(maxFoods)

  def gradual(maxFoods: Int = DEFAULT_MAX_FOODS, ratePerTick: Int = DEFAULT_RATE_PER_TICK): FoodRefillStrategy =
    GradualFoodRefillStrategy(maxFoods, ratePerTick)

  private[model] def generateFoods(count: Int, world: World): List[Food] =
    List.fill(count):
      val pos = world.generateRandomPosition()
      Food(s"food_${UUID.randomUUID().toString}", pos.x, pos.y, Food.DEFAULT_MASS)

case class ImmediateFoodRefillStrategy(maxFoods: Int = FoodRefillStrategy.DEFAULT_MAX_FOODS) extends FoodRefillStrategy:
  override def refill(world: World): World =
    val missing = maxFoods - world.foods.size
    if missing <= 0 then world
    else world.addFoods(FoodRefillStrategy.generateFoods(missing, world))

case class GradualFoodRefillStrategy(
  maxFoods: Int = FoodRefillStrategy.DEFAULT_MAX_FOODS,
  ratePerTick: Int = FoodRefillStrategy.DEFAULT_RATE_PER_TICK
) extends FoodRefillStrategy:
  override def refill(world: World): World =
    val missing = (maxFoods - world.foods.size).min(ratePerTick)
    if missing <= 0 then world
    else world.addFoods(FoodRefillStrategy.generateFoods(missing, world))

case object NoFoodRefillStrategy extends FoodRefillStrategy:
  override def refill(world: World): World = world
