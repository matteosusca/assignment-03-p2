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

  def removePlayers(playersToRemove: List[Player]): World =
    val idsToRemove = playersToRemove.map(_.id).toSet
    val newPlayers  = players.filterNot(p => idsToRemove.contains(p.id))
    copy(players = newPlayers)

  def removeFoods(foodsToRemove: List[Food]): World =
    val toRemoveSet = foodsToRemove.toSet
    val newFoods    = foods.filterNot(toRemoveSet.contains)
    copy(foods = newFoods)
    
  def generateRandomPosition(): Position =
    val x = scala.util.Random.nextDouble() * width
    val y = scala.util.Random.nextDouble() * height
    Position.of(x, y)
