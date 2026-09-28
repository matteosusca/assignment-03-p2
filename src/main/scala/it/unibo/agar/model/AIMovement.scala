package it.unibo.agar.model

/** Module to manage the AI movement in a simple Agar.IO system.
  */
object AIMovement:

  private def nearestFood(player: Player, world: World): Option[Food] =
    world.foods.minByOption(player.distanceTo)

  def moveAI(playerName: String, gameManager: GameStateManager): Unit =
    val world = gameManager.world
    for ai <- world.getPlayerById(playerName)
    do
      nearestFood(ai, world) match
        case Some(food) =>
          val dx       = food.x - ai.x
          val dy       = food.y - ai.y
          val distance = food.distanceTo(ai)
          if distance > 0 then gameManager.setPlayerDirection(playerName, dx / distance, dy / distance)
        case None =>
          // No Food, Stop the player movement
          gameManager.setPlayerDirection(playerName, 0, 0)
