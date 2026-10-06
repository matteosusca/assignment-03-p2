package it.unibo.agar.server

import it.unibo.agar.model.*
import it.unibo.agar.protocol.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ServerEngineTest extends AnyFunSuite with Matchers:

  private def createEngine(
    initialPlayers: List[Player] = List.empty,
    initialFoods: List[Food] = List.empty
  ): (ServerEngine, () => Option[WorldSnapshot]) =
    val world   = World(1000, 1000, initialPlayers, initialFoods)
    val manager = DefaultGameStateManager(world, FoodRefillStrategy.immediate(0))
    var lastSnapshot: Option[WorldSnapshot] = None
    val engine = ServerEngine(manager, snapshot => lastSnapshot = Some(snapshot))
    (engine, () => lastSnapshot)

  test("step should drain Join commands and broadcast a snapshot containing the new player"):
    val (engine, getLastSnapshot) = createEngine()

    engine.enqueueCommand(PlayerCommand.Join("p1"))
    engine.step()

    val snapshot = getLastSnapshot()
    snapshot.isDefined shouldBe true
    snapshot.get.players.map(_.id) should contain("p1")

  test("step should drain Move commands and update player position"):
    val player                    = Player("p1", 100.0, 100.0, Player.DEFAULT_MASS)
    val (engine, getLastSnapshot) = createEngine(initialPlayers = List(player))

    engine.enqueueCommand(PlayerCommand.Move("p1", 1.0, 0.0))
    engine.step()

    val snapshot = getLastSnapshot()
    snapshot.isDefined shouldBe true
    val movedPlayer = snapshot.get.players.find(_.id == "p1").get
    movedPlayer.x shouldBe (100.0 + DefaultGameStateManager.PLAYER_SPEED)
    movedPlayer.y shouldBe 100.0

  test("step should drain Leave commands and remove player from snapshot"):
    val player                    = Player("p1", 100.0, 100.0, Player.DEFAULT_MASS)
    val (engine, getLastSnapshot) = createEngine(initialPlayers = List(player))

    engine.enqueueCommand(PlayerCommand.Leave("p1"))
    engine.step()

    val snapshot = getLastSnapshot()
    snapshot.isDefined shouldBe true
    snapshot.get.players.exists(_.id == "p1") shouldBe false

  test("step should increment tickNumber on each call"):
    val (engine, getLastSnapshot) = createEngine()

    engine.step()
    getLastSnapshot().get.tickNumber shouldBe 1L

    engine.step()
    getLastSnapshot().get.tickNumber shouldBe 2L

  test("step should evict players whose inactivity exceeds the threshold"):
    var currentTime = 1000L
    val world = World(1000, 1000, List.empty, List.empty)
    val manager = new DefaultGameStateManager(world, FoodRefillStrategy.immediate(0))
    var lastSnapshot: Option[WorldSnapshot] = None
    val engine = ServerEngine(
      gameStateManager = manager,
      broadcastSnapshot = s => lastSnapshot = Some(s),
      inactivityThresholdMs = 1000L,
      timeProvider = () => currentTime
    )

    engine.enqueueCommand(PlayerCommand.Join("p1"))
    engine.step()
    lastSnapshot.get.players.map(_.id) should contain("p1")

    currentTime += 1500L
    engine.step()

    lastSnapshot.get.players.exists(_.id == "p1") shouldBe false

  test("heartbeat command should refresh player's last activity and prevent eviction"):
    var currentTime = 1000L
    val world = World(1000, 1000, List.empty, List.empty)
    val manager = new DefaultGameStateManager(world, FoodRefillStrategy.immediate(0))
    var lastSnapshot: Option[WorldSnapshot] = None
    val engine = ServerEngine(
      gameStateManager = manager,
      broadcastSnapshot = s => lastSnapshot = Some(s),
      inactivityThresholdMs = 1000L,
      timeProvider = () => currentTime
    )

    engine.enqueueCommand(PlayerCommand.Join("p1"))
    engine.step()
    lastSnapshot.get.players.map(_.id) should contain("p1")

    currentTime = 1600L
    engine.enqueueCommand(PlayerCommand.Heartbeat("p1"))
    engine.step()
    lastSnapshot.get.players.map(_.id) should contain("p1")

    currentTime = 2800L
    engine.step()
    lastSnapshot.get.players.exists(_.id == "p1") shouldBe false

  test("heartbeat for non-existent player should not track or create ghost entity"):
    val world = World(1000, 1000, List.empty, List.empty)
    val manager = new DefaultGameStateManager(world, FoodRefillStrategy.immediate(0))
    var lastSnapshot: Option[WorldSnapshot] = None
    val engine = ServerEngine(
      gameStateManager = manager,
      broadcastSnapshot = s => lastSnapshot = Some(s),
      inactivityThresholdMs = 1000L
    )

    engine.enqueueCommand(PlayerCommand.Heartbeat("ghost"))
    engine.step()
    lastSnapshot.get.players.exists(_.id == "ghost") shouldBe false