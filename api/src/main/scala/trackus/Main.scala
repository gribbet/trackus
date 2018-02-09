package trackus

import com.typesafe.scalalogging.LazyLogging
import org.http4s.server.blaze.BlazeBuilder
import trackus.database.DefaultDatabase
import trackus.queue.PositionTopic
import trackus.resource.Resources
import trackus.service.PositionService

import scala.concurrent.duration._
import scala.io.StdIn
import scalaz.concurrent.Task

object Main extends App with LazyLogging {

	Logging.start()

	implicit val executorService = Executor()
	implicit val httpClient = DefaultHttpClient()
	implicit val googleMetadata = GoogleMetadata()

	val server = for {

		database <- DefaultDatabase()

		positionService = {
			implicit val database_ = database

			new PositionService()
		}

		_ <- positionService.initialize

		positionTopic <- PositionTopic()

		_ <- positionTopic.start

		server <- {
			implicit val positionService_ = positionService
			implicit val positionTopic_ = positionTopic

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

		_ <- positionTopic.stop

	} yield server

	server.run

	executorService.shutdown
}

