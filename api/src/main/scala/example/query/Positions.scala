package example.query

import example.database.Driver.api._
import example.model.Position
import example.table.PositionTable
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

