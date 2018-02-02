package example.resource

import example.http.{CorsResponse, GZipFilter, LoggingFilter}
import example.service.PositionService

object Resources {
	def apply()(implicit
		positionService: PositionService) =

		(LoggingFilter
			andThen GZipFilter
			andThen CorsResponse) (

			PositionResource())
}
