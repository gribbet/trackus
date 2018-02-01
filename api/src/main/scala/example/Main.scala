package example

import com.typesafe.scalalogging.LazyLogging
import example.database.Database
import example.http.HttpServices
import example.service.PositionService
import fs2.util.Async
import fs2.{Strategy, Task, io}
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object Main extends App with LazyLogging {
	implicit val executorService = Executor()
	implicit val strategy = Strategy.fromExecutor(executorService)
	implicit val executionContext = ExecutionContext.fromExecutor(executorService)
	implicit val async = Async[Task]
	implicit val database = Database.database
	implicit val positionService = new PositionService()

	val server = for {
		_ <- positionService.initialize
		server <- BlazeBuilder
			.withExecutionContext(executionContext)
			.bindHttp(8080, "0.0.0.0")
			.withNio2(true)
			.mountService(HttpServices())
			.start
		_ <- io.stdinAsync(1024)
			.take(1)
			.run
		_ <- server.shutdown
	} yield server

	server.unsafeRun

	executorService.shutdown
}

