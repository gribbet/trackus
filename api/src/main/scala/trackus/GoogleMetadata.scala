package trackus

import com.typesafe.scalalogging.LazyLogging
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl._

import scalaz.concurrent.Task

trait GoogleMetadata {

	val connected: Task[Boolean]

	val instance: Task[String]

	val topic: Task[String]

	val database: Task[String]
}

object GoogleMetadata extends LazyLogging {

	def apply()(implicit client: Client) = {

		def get(path: String): Task[String] =
			Uri.fromString(s"http://metadata.google.internal/computeMetadata/v1/${path}")
				.fold(
					Task.fail,
					uri =>
						client.fetchAs[String](
							Request(
								Method.GET,
								uri,
								headers = Headers(Header("Metadata-Flavor", "Google")))))

		new GoogleMetadata {
			val connected = Task.now(true)

			val topic = Task.now("projects/elated-graph-193823/topics/trackus-pubsub-6-topic")

			val database = Task.now("35.226.201.104")

			val instance = Task.now("test")
		}

		/*new GoogleMetadata {

			lazy val connected: Task[Boolean] =
				get("")
					.map(_ => true)
					.handle {
						case _: UnresolvedAddressException => false
					}

			lazy val topic: Task[String] =
				get("instance/attributes/topic")

			lazy val database: Task[String] =
				get("instance/attributes/database")

			lazy val instance: Task[String] =
				get("instance/name")
		}*/
	}
}
