package it.unibo.agar.model

case class World(
  width: Int,
  height: Int,
  players: List[Player],
  foods: List[Food]
):
  def getWidth: Int            = width
  def getHeight: Int           = height
  def getPlayers: List[Player] = players
  def getFoods: List[Food]     = foods

  def getPlayersExcludingSelf(player: Player): List[Player] =
    players.filterNot(_.id == player.id)

  def getPlayerById(id: String): Option[Player] =
    players.find(_.id == id)
    
  def addPlayer(player: Player): World =
    copy(players = player :: players)

  def removePlayer(playerId: String): World =
    copy(players = players.filterNot(_.id == playerId))

  def removePlayers(playersToRemove: List[Player]): World =
    val idsToRemove = playersToRemove.map(_.id).toSet
    val newPlayers  = players.filterNot(p => idsToRemove.contains(p.id))
    copy(players = newPlayers)

  def addFoods(newFoods: List[Food]): World =
    copy(foods = foods ++ newFoods)

  def removeFoods(foodsToRemove: List[Food]): World =
    val toRemoveSet = foodsToRemove.toSet
    val newFoods    = foods.filterNot(toRemoveSet.contains)
    copy(foods = newFoods)
    
  def generateRandomPosition(): Position =
    Position.of(
      scala.util.Random.nextDouble() * width,
      scala.util.Random.nextDouble() * height
    )
