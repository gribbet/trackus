package trackus.queue

import scalaz.concurrent.Task
import scalaz.stream.Process

trait Topic[A] {

	def start: Task[Unit]

	def stop: Task[Unit]

	def publish(a: A): Task[Unit]

	def subscribe: Process[Task, A]
}