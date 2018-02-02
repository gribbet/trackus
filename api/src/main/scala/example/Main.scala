package example

import com.typesafe.scalalogging.LazyLogging
import example.database.Database
import example.http.HttpServices
import example.service.PositionService
import org.http4s.server.blaze.BlazeBuilder

import scalaz.concurrent.Task
import scalaz.stream.io

object Main extends App with LazyLogging {

	implicit val executorService = Executor()
	implicit val database = Database.database
	implicit val positionService = new PositionService()

	val server: Task[_] = for {

		_ <- positionService.initialize

		server <- BlazeBuilder
			.withServiceExecutor(executorService)
			.bindHttp(8080, "0.0.0.0")
			.enableHttp2(true)
			.withNio2(true)
			.mountService(HttpServices())
			.start

		_ <- io.stdInLines
			.take(1)
			.run

		_ <- server.shutdown

	} yield server

	server.run

	executorService.shutdown
}

