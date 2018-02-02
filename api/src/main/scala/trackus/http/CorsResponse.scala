package trackus.http

import org.http4s._
import org.http4s.dsl._

object CorsResponse extends HttpFilter {

	def apply(service: HttpService): HttpService = {
		val headers = Seq(
			Header("Access-Control-Allow-Origin", "*"),
			Header("Access-Control-Allow-Methods", "GET, POST"))

		Service.lift {
			_ match {
				case OPTIONS -> _ =>
					NoContent().putHeaders(headers: _*)
				case request@_ =>
					service(request).map(_.cata(_.putHeaders(headers: _*), Pass))
			}
		}
	}
}
