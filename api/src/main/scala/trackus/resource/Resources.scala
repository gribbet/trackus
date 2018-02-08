package trackus.resource

import org.http4s.server.syntax._
import trackus.http.{CorsResponse, GZipFilter, LoggingFilter}
import trackus.model.Position
import trackus.queue.Topic
import trackus.service.PositionService

object Resources {
	def apply()(implicit
		positionService: PositionService,
		positionTopic: Topic[Position]) =

		HealthResource() orElse (

			LoggingFilter andThen
				GZipFilter andThen
				CorsResponse) (

			PositionResource())
}
