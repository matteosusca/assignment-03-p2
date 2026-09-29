package it.unibo.agar.model

import scala.collection.mutable

class DefaultGameStateManager(initialWorld: World) extends GameStateManager:
  private var _world: World                                   = initialWorld
  private val playerDirections: mutable.Map[String, Position] = mutable.Map.empty

  _world.players.foreach(p => playerDirections.put(p.id, Position.ZERO))

  override def world: World = _world

  override def setPlayerDirection(playerId: String, dx: Double, dy: Double): Unit =
    if _world.getPlayerById(playerId).isDefined then playerDirections.put(playerId, Position.of(dx, dy))

  override def tick(): Unit =
    _world = handleEating(moveAllPlayers(_world))
    cleanupPlayerDirections()

  private def moveAllPlayers(currentWorld: World): World =
    val updatedPlayers = currentWorld.players.map { player =>
      val direction = playerDirections.getOrElse(player.id, Position.ZERO)
      val newX      = player.x + direction.x * DefaultGameStateManager.PLAYER_SPEED
      val newY      = player.y + direction.y * DefaultGameStateManager.PLAYER_SPEED
      player.moveTo(newX, newY)
    }
    currentWorld.copy(players = updatedPlayers)

  private def handleEating(currentWorld: World): World =
    val updatedPlayers  = currentWorld.players.map(player => growPlayer(currentWorld, player))
    val foodsToRemove   = currentWorld.players.flatMap(player => eatenFoods(currentWorld, player)).distinct
    val playersToRemove = currentWorld.players.flatMap(player => eatenPlayers(currentWorld, player)).distinct

    currentWorld
      .copy(players = updatedPlayers)
      .removeFoods(foodsToRemove)
      .removePlayers(playersToRemove)

  private def growPlayer(w: World, player: Player): Player =
    val afterFood = eatenFoods(w, player).foldLeft(player)((p, f) => p.grow(f))
    eatenPlayers(w, afterFood).foldLeft(afterFood)((p, other) => p.grow(other))

  private def eatenFoods(w: World, player: Player): List[Food] =
    w.foods.filter(food => EatingManager.canEatFood(player, food))

  private def eatenPlayers(w: World, player: Player): List[Player] =
    w.getPlayersExcludingSelf(player).filter(other => EatingManager.canEatPlayer(player, other))

  private def cleanupPlayerDirections(): Unit =
    val currentPlayerIds = _world.players.map(_.id).toSet
    playerDirections.filterInPlace((id, _) => currentPlayerIds.contains(id))
    _world.players.foreach(p => playerDirections.getOrElseUpdate(p.id, Position.ZERO))

  override def join(playerId: String): Unit =
    val pos = world.generateRandomPosition()
    join(playerId, pos)

  override def join(playerId: String, pos: Position, mass: Double = Player.DEFAULT_MASS): Unit =
    if _world.getPlayerById(playerId).isEmpty then
      val newPlayer = Player(playerId, pos.x, pos.y, mass)
      _world = _world.addPlayer(newPlayer)
      playerDirections.put(playerId, Position.ZERO)

  override def leave(playerId: String): Unit =
    _world = _world.removePlayer(playerId)
    playerDirections.remove(playerId)

object DefaultGameStateManager:
  val PLAYER_SPEED: Double = 2.0
  val MAX_FOOD_ITEMS: Int  = 150
