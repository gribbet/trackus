import sbt.Keys._
import sbt._
import sbtdocker._

val http4sVersion = "0.16.6"

lazy val project = Project(
	id = "api",
	base = file("."))
	.settings(
		version := "0.0.2",
		organization := "trackus",
		scalaVersion := "2.12.3",
		scalacOptions := Seq("-unchecked", "-deprecation"),
		libraryDependencies ++= Seq(
			"com.typesafe.slick" %% "slick" % "3.2.1",
			"com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
			"org.http4s" %% "http4s-blaze-server" % http4sVersion,
			"org.http4s" %% "http4s-dsl" % http4sVersion,
			"org.http4s" %% "http4s-circe" % http4sVersion,
			"io.circe" %% "circe-generic" % "0.8.0",
			"io.circe" %% "circe-parser" % "0.8.0",
			"org.slf4j" % "slf4j-simple" % "1.7.25",
			"com.h2database" % "h2" % "1.4.196"),
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
		})
	.enablePlugins(DockerPlugin)

