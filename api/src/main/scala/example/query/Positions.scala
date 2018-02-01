package example.query

import example.model.Position
import example.table.PositionTable

object Positions extends ModelTableQuery[Position, PositionTable](new PositionTable(_))