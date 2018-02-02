package trackus.service

import java.util.concurrent.ExecutorService

import com.typesafe.scalalogging.LazyLogging
import trackus.model.Position
import trackus.query.Positions
import slick.basic.BasicBackend
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext
import scalaz.\/
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.{Process, async}

class PositionService(implicit
	database: BasicBackend#DatabaseDef,
	executor: ExecutorService) extends LazyLogging {

	implicit val strategy = Strategy.Executor(executor)
	implicit val executionContext =
		ExecutionContext.fromExecutorService(executor)

	private val topic = async.topic[Position]()

	def initialize() =
		task(Positions.initialize)

	def create(position: Position): Task[Unit] =
		for {
			position <- task(Positions.insert(position))
			_ <- topic.publishOne(position)
		} yield logger.info(s"Created ${position}")

	def stream(): Process[Task, Position] =
		Process.eval(list()).flatMap(Process.emitAll) ++ topic.subscribe

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