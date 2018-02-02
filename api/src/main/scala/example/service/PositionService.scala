package example.service

import java.util.concurrent.ExecutorService

import example.model.Position
import example.query.Positions
import slick.basic.BasicBackend
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext
import scalaz.\/
import scalaz.concurrent.Task
import scalaz.stream.{Process, async}

class PositionService(implicit
	database: BasicBackend#DatabaseDef,
	executor: ExecutorService) {

	implicit val executionContext =
		ExecutionContext.fromExecutorService(executor)

	private val topic = async.topic[Position]()

	def initialize() =
		task(Positions.initialize)

	def create(position: Position): Task[Unit] =
		for {
			position <- task(Positions.insert(position))
			_ <- topic.publishOne(position)
		} yield ()

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