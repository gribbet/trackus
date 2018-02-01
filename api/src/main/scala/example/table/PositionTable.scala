package example.table

import example.database.Driver.api._
import example.model.Position

class PositionTable(tag: Tag) extends ModelTable[Position](tag, "position") {

	def * = (id, user, longitude, latitude, timestamp) <> (Position.tupled, Position.unapply _)

	def user = column[String]("user")

	def longitude = column[Double]("longitude")

	def latitude = column[Double]("latitude")

	def timestamp = column[Long]("timestamp")
}

