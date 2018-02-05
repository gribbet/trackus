package trackus

import com.typesafe.scalalogging.LazyLogging
import org.http4s.server.blaze.BlazeBuilder
import trackus.database.Database
import trackus.resource.Resources
import trackus.service.PositionService
import scala.concurrent.duration._

import scala.io.StdIn
import scalaz.concurrent.Task

object Main extends App with LazyLogging {
	Logging.start()

	implicit val executorService = Executor()
	implicit val database = Database.database
	implicit val positionService = new PositionService()

	val server = for {

		_ <- positionService.initialize

		server <- BlazeBuilder
			.withServiceExecutor(executorService)
			.bindHttp(8080, "0.0.0.0")
			.withNio2(true)
			.withIdleTimeout(1.day)
			.mountService(Resources())
			.start

		line <- Task.delay(StdIn.readLine())

		_ <-
			if (line != null)
				server.shutdown
			else
				Task.async[Nothing](_ => ())

	} yield server

	server.run

	executorService.shutdown
}

