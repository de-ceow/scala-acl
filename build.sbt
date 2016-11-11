lazy val commonSettings = Seq(
	organization := "de.ceow",
	version := "1.1.0",
	scalaVersion := "2.11.8",
  useGpg := true,
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Ydelambdafy:method",
    "-Yno-adapted-args",
    "-Yrangepos",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  credentials in Scaladex += Credentials(Path.userHome / ".ivy2" / ".scaladex.credentials"),
  scaladexKeywords := Seq("web", "playframework", "acl", "security", "authentication"),
  pomExtra := (
    <url>http://github.com/de-ceow</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/bsd-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:de-ceow/scala-acl.git</url>
        <connection>scm:git@github.com:de-ceow/scala-acl.git</connection>
      </scm>
      <developers>
        <developer>
          <id>rmarske</id>
          <name>Ronald Marske</name>
          <url>http://github.com/Scyks</url>
        </developer>
      </developers>)
)

lazy val acl = (project in file("acl")).
  settings(commonSettings).
  settings(
    name := "scala-acl",
    crossScalaVersions := Seq("2.11.8", "2.12.0"),
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "3.8.6" % Test,
      "org.slf4j" % "slf4j-simple" % "1.7.2" % Test
    )
  )

lazy val play = (project in file("play")).
  settings(commonSettings).
  settings(
    scalaVersion := "2.11.8",
    name := "play-acl",
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "3.8.6" % Test,
      "org.slf4j" % "slf4j-simple" % "1.7.2" % Test,
      "com.typesafe.play" %% "play-specs2" % "2.5+" % Test,
      "com.typesafe.play" %% "play-test" % "2.5+" % Test,
      "com.typesafe.play" %% "play" % "2.5+"
    )
  ).
  dependsOn(acl)

lazy val root = (project in file(".")).aggregate(acl, play).settings(
  scalaVersion := "2.11.8",
  publish := (),
  publishLocal := (),
  publishArtifact := false
)


