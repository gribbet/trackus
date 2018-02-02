package example.http

import io.circe._
import io.circe.syntax._
import org.http4s.server.websocket.WS
import org.http4s.websocket.WebsocketBits.{Text, WebSocketFrame}

import scalaz.concurrent.Task
import scalaz.stream.{Exchange, Process, Sink, process1}

object StreamSocket {

	def apply[T](
		read: Process[Task, T],
		write: Sink[Task, T])
		(implicit
			encoder: Encoder[T],
			decoder: Decoder[T]) =

		WS(Exchange(
			read
				.map(_.asJson)
				.map(_.toString)
				.map(Text(_)),
			write.pipeIn[WebSocketFrame] {
				process1.lift[WebSocketFrame, Json] {
					case Text(text, _) => parser.parse(text).getOrElse(Json.Null)
					case _ => Json.Null
				}.flatMap {
					decoder.decodeJson(_) match {
						case Right(t) => Process(t)
						case _ => Process.empty
					}
				}
			}))
}