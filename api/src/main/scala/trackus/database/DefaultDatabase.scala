package trackus.database

import java.util.concurrent.ExecutorService

import slick.jdbc.JdbcProfile
import trackus.GoogleMetadata

import scalaz.concurrent.Task

object DefaultDatabase {

	def apply()(implicit
		executorService: ExecutorService,
		googleMetadata: GoogleMetadata
	): Task[Database[_ <: JdbcProfile]] =

		googleMetadata.connected
			.map(connected =>
				if (connected)
					GoogleDatabase()
				else
					MemoryDatabase())
}
