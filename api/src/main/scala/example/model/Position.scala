package example.model

case class Position(
	val id: Option[Long],
	val user: String,
	val longitude: Double,
	val latitude: Double,
	val timestamp: Long)
