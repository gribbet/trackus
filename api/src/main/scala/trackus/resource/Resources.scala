package trackus.resource

import trackus.http.{CorsResponse, GZipFilter, LoggingFilter}
import trackus.service.PositionService

import org.http4s.server.syntax._

object Resources {
	def apply()(implicit
		positionService: PositionService) =

		(LoggingFilter andThen
			GZipFilter andThen
			CorsResponse) (

			PositionResource() orElse
				HealthResource())
}
