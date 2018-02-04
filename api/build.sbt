import sbt.Keys._
import sbt._
import sbtdocker._

val http4sVersion = "0.16.6"

lazy val project = Project(
	id = "api",
	base = file("."))
	.settings(

		version := "0.0.3",
		organization := "trackus",
		scalaVersion := "2.12.3",

		scalacOptions := Seq("-unchecked", "-deprecation"),

		libraryDependencies ++= Seq(
			"com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
			"com.typesafe.slick" %% "slick" % "3.2.1",
			"io.circe" %% "circe-generic" % "0.8.0",
			"io.circe" %% "circe-parser" % "0.8.0",
			"org.http4s" %% "http4s-blaze-server" % http4sVersion,
			"org.http4s" %% "http4s-circe" % http4sVersion,
			"org.http4s" %% "http4s-dsl" % http4sVersion,
			"com.google.cloud" % "google-cloud-logging-logback" % "0.33.0-alpha",
			"com.h2database" % "h2" % "1.4.196"),

		buildInfoPackage := "trackus.build",

		dockerfile in docker := {
			val artifact: File = assembly.value
			val artifactTargetPath = s"/app/${artifact.name}"

			new Dockerfile {
				from("alpine:3.7")
				run("apk", "add", "--update", "openjdk8")
				workDir("/data")
				expose(8080)
				add(artifact, artifactTargetPath)
				entryPoint("java", "-jar", artifactTargetPath)
			}
		},

		assemblyMergeStrategy in assembly := {
			case "META-INF/io.netty.versions.properties" => MergeStrategy.discard
			case x =>
				val oldStrategy = (assemblyMergeStrategy in assembly).value
				oldStrategy(x)
		})
	.enablePlugins(DockerPlugin)
	.enablePlugins(BuildInfoPlugin)

