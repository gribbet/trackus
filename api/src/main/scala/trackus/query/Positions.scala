package trackus.query

import trackus.database.Driver.api._
import trackus.model.Position
import trackus.table.PositionTable
import slick.lifted.TableQuery

object Positions extends TableQuery(new PositionTable(_)) {

	def initialize() =
		this.schema.create

	def list() =
		this.result

	def insert(position: Position) =
		(this returning map(_.id)
			into ((position, id) =>
				position.copy(id = Some(id)))) += position
}

