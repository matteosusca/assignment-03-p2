package it.unibo.agar.protocol

import upickle.default.ReadWriter

enum PlayerCommand derives ReadWriter:
	// the override is necessary due to the promise.
	// just like an interface in Java, the playerId is a promise that all commands have a playerId
	case Join(override val playerId: String)
	case Move(override val playerId: String, dx: Double, dy: Double)
	case Leave(override val playerId: String)

	// promise that all commands have a playerId
	def playerId: String