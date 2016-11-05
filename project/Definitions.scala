import sbt._

object Definitions {

  val scalacOptions = Seq(
    "-deprecation",
    "-feature",
    "-encoding", "Utf8"
  )

  val libraryDependencies = Seq(
    "org.specs2" %% "specs2-core" % "3.8.6" % Test,
    "org.slf4j" % "slf4j-simple" % "1.7.2" % Test
  )
}