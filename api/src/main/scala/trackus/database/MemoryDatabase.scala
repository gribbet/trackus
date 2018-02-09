package trackus.database

import java.util.concurrent.ExecutorService

import slick.jdbc.H2Profile
import slick.util.AsyncExecutor

import scala.concurrent.ExecutionContext

object MemoryDatabase {

	def apply()(implicit
		executorService: ExecutorService) =

		new Database[H2Profile] {
			val profile = H2Profile
			val database = H2Profile.api.Database.forURL(
				"jdbc:h2:mem:example;DB_CLOSE_DELAY=-1",
				driver = "org.h2.Driver",
				executor = new AsyncExecutor() {
					override def executionContext: ExecutionContext =
						ExecutionContext.fromExecutorService(executorService)

					override def close() = Unit
				})
		}
}
