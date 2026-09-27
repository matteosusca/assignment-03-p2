package it.unibo.agar.model

import scala.util.Random

object GameInitializer:
  private val random: Random = Random()

  def initialPlayers(numPlayers: Int, width: Int, height: Int, initialMass: Double): List[Player] =
    (1 to numPlayers).map { i =>
      Player(s"p$i", random.nextInt(width).toDouble, random.nextInt(height).toDouble, initialMass)
    }.toList

  def initialPlayers(numPlayers: Int, width: Int, height: Int): List[Player] =
    initialPlayers(numPlayers, width, height, 120.0)

  def initialFoods(numFoods: Int, width: Int, height: Int, initialMass: Double): List[Food] =
    (1 to numFoods).map { i =>
      Food(s"f$i", random.nextInt(width).toDouble, random.nextInt(height).toDouble, initialMass)
    }.toList

  def initialFoods(numFoods: Int, width: Int, height: Int): List[Food] =
    initialFoods(numFoods, width, height, Food.DEFAULT_MASS)
