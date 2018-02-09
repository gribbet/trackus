package trackus

import java.util.concurrent.ExecutorService

import org.http4s.client.blaze.{BlazeClientConfig, PooledHttp1Client}

object DefaultHttpClient {

	def apply()(implicit
		executorService: ExecutorService) =
		PooledHttp1Client(
			config = BlazeClientConfig.defaultConfig.copy(
				customExecutor = Some(executorService)))
}
