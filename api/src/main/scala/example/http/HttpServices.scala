package example.http

import example.service.PositionService

object HttpServices {
	def apply()(implicit
		positionService: PositionService) =

		(LoggingFilter
			andThen GZipFilter
			andThen CorsResponse) (

			PositionResource())
}
