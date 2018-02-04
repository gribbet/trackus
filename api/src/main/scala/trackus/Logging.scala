package trackus

import ch.qos.logback.classic.jul.LevelChangePropagator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Level, LoggerContext, PatternLayout}
import ch.qos.logback.core.ConsoleAppender
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.ServiceOptions
import com.google.cloud.logging.logback.LoggingAppender
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory
import trackus.build.BuildInfo

import scala.util.Try

object Logging {
	private val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

	def start() = {
		Thread.setDefaultUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler)

		val levelChangePropagator: LevelChangePropagator = new LevelChangePropagator
		levelChangePropagator.setContext(context)
		context.addListener(levelChangePropagator)

		val pattern = new PatternLayout
		pattern.setContext(context)
		pattern.setPattern("%date{ISO8601} %-5level %msg [%logger] [%thread]%n")
		pattern.start

		val logger = context.getLogger("ROOT")
		logger.detachAndStopAllAppenders
		logger.setLevel(Level.INFO)

		val console: ConsoleAppender[ILoggingEvent] = new ConsoleAppender[ILoggingEvent]
		console.setContext(context)
		console.setLayout(pattern)
		console.start
		logger.addAppender(console)

		Try(GoogleCredentials.getApplicationDefault).toOption.map { _ =>
			val google: LoggingAppender = new LoggingAppender()
			google.setContext(context)
			google.setLog("trackus")
			google.start()
			logger.addAppender(google)
		}

		context.getLogger("org.http4s.blaze.channel.ServerChannelGroup").setLevel(Level.WARN)

		logger.info(s"Running ${BuildInfo.name} ${BuildInfo.version}")
	}

	def stop() = {
		context.stop()
	}

	private class DefaultUncaughtExceptionHandler extends Thread.UncaughtExceptionHandler with LazyLogging {
		override def uncaughtException(t: Thread, e: Throwable) = {
			logger.error("Unexpected failure", e)
			Thread.sleep(2000)
		}
	}

}
