package it.unibo.agar.model

import it.unibo.agar.protocol.{FoodSnapshot, PlayerSnapshot}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class GameStateManagerSnapshotTest extends AnyFunSuite with Matchers:

  private def createManager(players: List[Player] = List.empty, foods: List[Food] = List.empty): GameStateManager =
    DefaultGameStateManager(World(1000, 1000, players, foods))

  test("toSnapshot should correctly give tick 0 when no ticks have been performed"):
    val manager  = createManager()
    val snapshot = manager.toSnapshot()

    snapshot.tickNumber shouldBe 0

  test("toSnapshot should correctly give n tick after n ticks have been performed"):
    val manager = createManager()
    (1 to 5).foreach(_ => manager.tick())
    val snapshot = manager.toSnapshot()

    snapshot.tickNumber shouldBe 5

  test("toSnapshot should correctly contain all players in the world"):
    val players = List(
      Player("player1", 110, 120, 100),
      Player("player2", 210, 220, 200)
    )
    val manager  = createManager(players = players)
    val snapshot = manager.toSnapshot()

    val p1Snapshot = PlayerSnapshot("player1", 110, 120, 100)
    val p2Snapshot = PlayerSnapshot("player2", 210, 220, 200)
    val playerSnapshots = List(p1Snapshot, p2Snapshot)
    snapshot.players should contain theSameElementsAs playerSnapshots
    
  test("toSnapshot should correctly contain all foods in the world"):
    val foods = List(
      Food("food1", 310, 320, 10),
      Food("food2", 410, 420, 20)
    )
    val manager  = createManager(foods = foods)
    val snapshot = manager.toSnapshot()
    
    val f1Snapshot = FoodSnapshot("food1", 310, 320)
    val f2Snapshot = FoodSnapshot("food2", 410, 420)
    val foodSnapshots = List(f1Snapshot, f2Snapshot)
    snapshot.foods should contain theSameElementsAs foodSnapshots