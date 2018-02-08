package trackus.queue

import java.util.concurrent.ExecutorService

import scalaz.concurrent.{Strategy, Task}
import scalaz.stream
import scalaz.stream.async

class LocalTopic[A](implicit
	executor: ExecutorService)
	extends Topic[A] {

	private implicit val strategy = Strategy.Executor(executor)

	private val topic = async.topic[A]()

	def start: Task[Unit] = Task(())

	def stop: Task[Unit] = Task(())

	def publish(a: A): Task[Unit] =
		topic.publishOne(a)

	def subscribe: stream.Process[Task, A] =
		topic.subscribe
}
