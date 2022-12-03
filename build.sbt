ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.1"

val ceVersion = "3.4.2"
val fs2Version = "3.4.0"
val http4sVersion = "1.0.0-M37"

lazy val root = (project in file("."))
  .settings(
    name := "http4s-bug",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.4.2",
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,

      "org.slf4j" % "slf4j-simple" % "2.0.5"
    )
  )
