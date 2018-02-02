package trackus.resource

import trackus.http.StreamSocket
import trackus.model.Position
import trackus.service.PositionService
import io.circe._
import io.circe.generic.auto._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl._

import scalaz.stream.sink


object PositionResource {

	implicit val positionEncoder = Encoder[Position]
	implicit val positionDecoder = Decoder[Position]

	def apply()(implicit
		positionService: PositionService) = HttpService {

		case GET -> Root =>
			StreamSocket[Position](
				positionService.stream,
				sink.lift(positionService.create(_)))
	}
}
