package trackus.resource

import org.http4s.HttpService
import org.http4s.dsl._


object HealthResource {
	def apply() = HttpService {
		case GET -> Root / "health" => Ok()
	}
}