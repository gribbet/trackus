package example.http

import example.json.Codecs._
import example.model.Position
import example.service.PositionService

object PositionResource {
	def apply()(implicit positionService: PositionService) =
		Path("positions", ModelResource[Position]())
}
