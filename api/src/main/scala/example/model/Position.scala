package example.model

import java.util.UUID

case class Position(
	val id: UUID,
	val user: String,
	val longitude: Double,
	val latitude: Double,
	val timestamp: Long)
	extends Model
