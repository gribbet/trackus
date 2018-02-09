package trackus.resource

import io.circe._
import io.circe.generic.auto._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl._
import trackus.http.StreamSocket
import trackus.model.Position
import trackus.queue.Topic
import trackus.service.PositionService

import scalaz.stream.sink


object PositionResource {

	implicit val positionEncoder = Encoder[Position]
	implicit val positionDecoder = Decoder[Position]

	def apply()(implicit
		positionService: PositionService,
		positionTopic: Topic[Position]
	) = HttpService {

		case GET -> Root / "positions" =>
			StreamSocket[Position](
				positionService.stream ++ positionTopic.subscribe,
				sink.lift(position =>
					for {
						position <- positionService.create(position)
						_ <- positionTopic.publish(position)
					} yield ()))
	}
}
