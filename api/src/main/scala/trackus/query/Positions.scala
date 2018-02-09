package trackus.query

import trackus.database.Database
import trackus.model.Position

class Positions(implicit
	val database: Database[_]) {

	import database.profile.api._

	class PositionsTable(tag: Tag)
		extends Table[Position](tag, "position") {

		def * = (id.?, user, longitude, latitude, timestamp) <> (Position.tupled, Position.unapply _)

		def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

		def user = column[String]("user")

		def longitude = column[Double]("longitude")

		def latitude = column[Double]("latitude")

		def timestamp = column[Long]("timestamp")
	}

	lazy val positions = TableQuery[PositionsTable]

	lazy val initialize =
		positions.schema.create

	lazy val list =
		positions.result

	def insert(position: Position) =
		(positions returning positions.map(_.id)
			into ((position, id) =>
			position.copy(id = Some(id)))) += position
}

object Positions {
	def apply()(implicit
		database: Database[_]) =
		new Positions()
}

