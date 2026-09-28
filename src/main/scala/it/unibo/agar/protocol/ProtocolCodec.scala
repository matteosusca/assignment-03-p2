package it.unibo.agar.protocol

import upickle.default.*

import java.nio.charset.StandardCharsets
import scala.util.Try

object ProtocolCodec:

	// encodes any message that has a ReadWriter into a byte array
	def encode[T: ReadWriter](message: T): Array[Byte] =
		write(message).getBytes(StandardCharsets.UTF_8)

	// decodes a byte array into a message of type T that has a ReadWriter
	def decode[T: ReadWriter](bytes: Array[Byte]): Try[T] =
		Try:
			val json = new String(bytes, StandardCharsets.UTF_8)
			read[T](json)