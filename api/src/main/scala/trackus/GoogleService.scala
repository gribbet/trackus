package trackus

import java.nio.channels.UnresolvedAddressException

import com.typesafe.scalalogging.LazyLogging
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl._

import scalaz.concurrent.Task

trait GoogleService {

	def connected: Task[Boolean]

	def instance(): Task[String]

	def topic(): Task[String]
}

object GoogleService extends LazyLogging {

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

		new GoogleService {

			def connected(): Task[Boolean] =
				get("")
					.map(_ => true)
					.handle {
						case _: UnresolvedAddressException => false
					}

			def topic(): Task[String] =
				get("instance/attributes/topic")

			def instance(): Task[String] =
				get("instance/name")
		}
	}
}
