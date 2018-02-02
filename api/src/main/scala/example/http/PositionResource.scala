package example.http

import example.model.Position
import example.service.PositionService
import io.circe._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

object PositionResource {

	implicit val positionEncoder = Encoder[Position]
	implicit val positionDecoder = Decoder[Position]

	implicit val positionEntityEncoder = jsonEncoderOf[Position]
	implicit val positionEntityDecoder = jsonOf[Position]

	def apply()(implicit
		positionService: PositionService) = HttpService {

		case GET -> Root =>
			Ok(positionService.stream())

		case request@POST -> Root =>
			request.decode[Position] { position =>
				positionService.create(position)
					.flatMap(_ => Created())
			}
	}
}
