package trackus

import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.client.blaze.{BlazeClientConfig, PooledHttp1Client}
import org.http4s.server.blaze.BlazeBuilder
import trackus.database.Database
import trackus.model.Position
import trackus.queue.{GoogleTopic, LocalTopic}
import trackus.resource.Resources
import trackus.service.PositionService

import scala.concurrent.duration._
import scala.io.StdIn
import scalaz.concurrent.Task

object Main extends App with LazyLogging {

	Logging.start()

	implicit val executorService = Executor()
	implicit val database = Database.database
	implicit val httpClient = PooledHttp1Client(
		config = BlazeClientConfig.defaultConfig.copy(
			customExecutor = Some(executorService)))
	implicit val googleService = GoogleService()
	implicit val positionService = new PositionService
	implicit val positionEncoder = Encoder[Position]
	implicit val positionDecoder = Decoder[Position]


	val server = for {

		connected <- googleService.connected

		_ = logger.debug("Connected: " + connected)

		topic <-
			if (connected)
				googleService.topic()
					.flatMap(topic =>
						GoogleTopic[Position](topic))
			else
				Task.now(new LocalTopic[Position])

		_ <- topic.start

		_ <- positionService.initialize

		server <- {
			implicit val positionTopic = topic

			BlazeBuilder
				.withServiceExecutor(executorService)
				.bindHttp(8080, "0.0.0.0")
				.withNio2(true)
				.withIdleTimeout(1.day)
				.mountService(Resources())
				.start
		}

		line <- Task.delay(StdIn.readLine())

		_ <-
			if (line != null)
				server.shutdown
			else
				Task.async[Nothing](_ => ()) // TODO: Fix shutdown behavior

		_ <- topic.stop

	} yield server

	server.run

	executorService.shutdown
}

