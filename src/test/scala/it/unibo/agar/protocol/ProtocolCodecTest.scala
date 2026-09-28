package it.unibo.agar.protocol

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import upickle.default.ReadWriter
import scala.util.Success

class ProtocolCodecTest extends AnyFunSuite with Matchers:

  private def roundtrip[T: ReadWriter](message: T): Unit =
    ProtocolCodec.decode[T](ProtocolCodec.encode(message)) shouldBe Success(message)

  test("should roundtrip all PlayerCommands"):
    Seq(
      PlayerCommand.Join("p1"),
      PlayerCommand.Move("p1", 1.5, -0.8),
      PlayerCommand.Leave("p1")
    ).foreach(roundtrip)

  test("should roundtrip WorldSnapshot in active and ended states"):
    roundtrip(
      WorldSnapshot(
        tickNumber = 42L,
        players = Seq(PlayerSnapshot("p1", 100.0, 200.0, 15.0, 200.0)),
        foods = Seq(FoodSnapshot("f1", 150.0, 250.0, 5.0)),
        isGameOver = false,
        winnerId = None
      )
    )
    roundtrip(
      WorldSnapshot(
        tickNumber = 100L,
        players = Seq(PlayerSnapshot("p1", 500.0, 500.0, 30.0, 1050.0)),
        foods = Seq.empty,
        isGameOver = true,
        winnerId = Some("p1")
      )
    )

  test("should fail when decoding invalid payloads"):
    ProtocolCodec.decode[WorldSnapshot]("invalid json".getBytes).isFailure shouldBe true