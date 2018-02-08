package trackus.service

import java.util.concurrent.ExecutorService

import com.typesafe.scalalogging.LazyLogging
import slick.basic.BasicBackend
import slick.dbio.DBIO
import trackus.model.Position
import trackus.query.Positions

import scala.concurrent.ExecutionContext
import scalaz.\/
import scalaz.concurrent.Task
import scalaz.stream.Process

class PositionService(implicit
	database: BasicBackend#DatabaseDef,
	executor: ExecutorService) extends LazyLogging {

	implicit val executionContext =
		ExecutionContext.fromExecutorService(executor)

	def initialize() =
		task(Positions.initialize)

	def create(position: Position): Task[Position] =
		task(Positions.insert(position))
			.map(position => {
				logger.info(s"Created ${position}")
				position
			})

	def stream(): Process[Task, Position] =
		Process.eval(list()).flatMap(Process.emitAll)

	def list(): Task[List[Position]] =
		task(Positions.list.map(_.toList))

	private def task[R](action: DBIO[R]): Task[R] =
		Task.async { cb =>
			database
				.run(action)
				.onComplete(x =>
					cb(\/.fromEither(x.toEither)))
		}
}