package trackus.queue

import java.util.concurrent.ExecutorService

import io.circe._
import io.circe.generic.auto._
import trackus.GoogleMetadata
import trackus.model.Position

import scalaz.concurrent.Task

object PositionTopic {
	def apply()(implicit
		executorService: ExecutorService,
		googleMetadata: GoogleMetadata
	): Task[Topic[Position]] = {

		implicit val positionEncoder = Encoder[Position]
		implicit val positionDecoder = Decoder[Position]

		googleMetadata.connected
			.flatMap(connected =>
				if (connected)
					googleMetadata.topic
						.flatMap(topic =>
							GoogleTopic[Position](topic))
				else
					Task.now(new LocalTopic[Position]))
	}

}
