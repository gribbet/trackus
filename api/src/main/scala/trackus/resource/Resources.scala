package trackus.resource

import trackus.http.{CorsResponse, GZipFilter, LoggingFilter}
import trackus.service.PositionService

object Resources {
	def apply()(implicit
		positionService: PositionService) =

		(LoggingFilter
			andThen GZipFilter
			andThen CorsResponse) (

			PositionResource())
}
