package trackus.queue

import java.util.concurrent.ExecutorService

import com.google.api.gax.rpc.AlreadyExistsException
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.{AckReplyConsumer, Publisher, Subscriber, SubscriptionAdminClient}
import com.google.protobuf.ByteString
import com.google.pubsub.v1.{PubsubMessage, PushConfig, SubscriptionName, TopicName}
import com.typesafe.scalalogging.LazyLogging
import io.circe.{Decoder, Encoder, Json, parser}
import trackus.GoogleService

import scala.util.Try
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.{Process, async}

object GoogleTopic extends LazyLogging {

	def apply[A](name: String)(implicit
		service: GoogleService,
		executor: ExecutorService,
		encoder: Encoder[A],
		decoder: Decoder[A]): Task[Topic[A]] = {

		implicit val strategy = Strategy.Executor(executor)

		val projectId = ServiceOptions.getDefaultProjectId
		val topic = TopicName.parse(name)
		val publisher = Publisher.newBuilder(topic).build()
		val queue = async.unboundedQueue[A]

		for {
			instance <- service.instance()

			subscription = SubscriptionName.of(projectId, s"${topic.getTopic}-${instance}")

			_ = Try(SubscriptionAdminClient.create()
				.createSubscription(
					subscription,
					topic,
					PushConfig.getDefaultInstance,
					10))
				.map { _ =>
					logger.info(s"Subscription created: ${subscription.getSubscription}")
				}
				.recover {
					case _: AlreadyExistsException =>
						logger.warn("Subscription already exists")
				}
				.get

			subscriber = Subscriber.newBuilder(subscription,

				(message: PubsubMessage,
					consumer: AckReplyConsumer) => {

					val json = parser.parse(message.getData.toStringUtf8)
						.getOrElse(Json.Null)

					Task.fork(decoder
						.decodeJson(json)
						.map(queue.enqueueOne(_)
							.flatMap(_ => Task(consumer.ack())))
						.getOrElse(Task(()))).run
				})
				.build()

		} yield new Topic[A] {

			def start = Task {
				subscriber.startAsync()
				subscriber.awaitRunning()
			}

			def stop = Task {
				subscriber.stopAsync().awaitTerminated()
				SubscriptionAdminClient.create()
					.deleteSubscription(subscriber.getSubscriptionName)
				logger.info("Subscription deleted")
			}

			def publish(a: A): Task[Unit] = Task {
				val json = encoder.apply(a)
				val data = ByteString.copyFromUtf8(json.toString())
				val id = publisher.publish(
					PubsubMessage
						.newBuilder
						.setData(data)
						.build).get()
				logger.debug(s"Message published: ${id}")
			}

			def subscribe: Process[Task, A] =
				queue.dequeue
		}
	}
}