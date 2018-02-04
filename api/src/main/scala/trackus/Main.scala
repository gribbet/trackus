package trackus

import com.typesafe.scalalogging.LazyLogging
import trackus.database.Database
import trackus.resource.Resources
import trackus.service.PositionService
import org.http4s.server.blaze.BlazeBuilder

import scalaz.stream.io

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
			.mountService(Resources())
			.start

		_ <- io.stdInLines
			.take(1)
			.run

		_ <- server.shutdown

	} yield server

	server.run

	executorService.shutdown
}

