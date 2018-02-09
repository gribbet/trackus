package trackus.database

import java.util.concurrent.ExecutorService

import slick.jdbc.PostgresProfile
import slick.util.AsyncExecutor

import scala.concurrent.ExecutionContext

object GoogleDatabase {

	def apply()(implicit
		executorService: ExecutorService) =

		new Database[PostgresProfile] {
			val profile = PostgresProfile
			val database = PostgresProfile.api.Database.forURL(
				s"jdbc:postgresql://sql_proxy/trackus",
				user = "trackus",
				password = "trackus",
				driver = "slick.jdbc.PostgresProfile$",
				executor = new AsyncExecutor() {
					override def executionContext: ExecutionContext =
						ExecutionContext.fromExecutorService(executorService)

					override def close() = Unit
				})
		}

}
