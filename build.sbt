ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "agar-io",
    Compile / mainClass := Some("it.unibo.agar.Main"),
    fork := true,
    libraryDependencies ++= Seq(
      "com.rabbitmq" % "amqp-client" % "5.25.0",
      "com.lihaoyi" %% "upickle" % "4.0.2",
      "org.slf4j" % "slf4j-simple" % "2.0.16",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    )
  )
