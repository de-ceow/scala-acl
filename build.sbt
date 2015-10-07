name := "play-acl"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-encoding", "Utf8"
)

libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % "2.4.3",
    "org.specs2" %% "specs2-core" % "3.6.4" % "test"
)
