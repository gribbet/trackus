package trackus.service

import java.util.concurrent.ExecutorService

import com.typesafe.scalalogging.LazyLogging
import slick.dbio.DBIO
import trackus.database.Database
import trackus.model.Position
import trackus.query.Positions

import scala.concurrent.ExecutionContext
import scalaz.\/
import scalaz.concurrent.Task
import scalaz.stream.Process

class PositionService(implicit
	database: Database[_],
	executor: ExecutorService)
	extends LazyLogging {

	implicit private val executionContext =
		ExecutionContext.fromExecutorService(executor)

	private val positions =
		Positions()

	def initialize() =
		task(positions.initialize).handle {
			case e: Exception => ()
		}

	def create(position: Position): Task[Position] =
		task(positions.insert(position))
			.map(position => {
				logger.info(s"Created ${position}")
				position
			})

	def stream(): Process[Task, Position] =
		Process.eval(list()).flatMap(Process.emitAll)

	def list(): Task[List[Position]] =
		task(positions.list.map(_.toList))

	private def task[R](action: DBIO[R]): Task[R] =
		Task.async { cb =>
			database.database
				.run(action)
				.onComplete(x =>
					cb(\/.fromEither(x.toEither)))
		}
}