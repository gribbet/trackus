package example

import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import example.database.Database
import example.resource.Resources
import example.service.PositionService
import org.http4s.server.blaze.BlazeBuilder

import scalaz.concurrent.Task
import scalaz.stream.io

object Main extends App with LazyLogging {

	implicit val executorService = Executor()
	implicit val database = Database.database
	implicit val positionService = new PositionService()

	val keypath = Paths.get("server.jks").toAbsolutePath().toString()

	val server: Task[_] = for {

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

