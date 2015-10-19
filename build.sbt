name := "playacl"

version := "0.3.0"

organization := "com.github.scyks"

scalaVersion := "2.11.7"

useGpg := true

pomExtra := (
	<url>http://github.com/Scyks</url>
	<licenses>
		<license>
			<name>BSD-style</name>
			<url>http://www.opensource.org/licenses/bsd-license.php</url>
			<distribution>repo</distribution>
		</license>
	</licenses>
	<scm>
		<url>git@github.com:scyks/play-scala.git</url>
		<connection>scm:git@github.com:scyks/play-scala.git</connection>
	</scm>
	<developers>
		<developer>
			<id>rmarske</id>
			<name>Ronald Marske</name>
			<url>http://github.com/Scyks</url>
		</developer>
	</developers>)

scalacOptions ++= Seq(
	"-deprecation",
	"-feature",
	"-encoding", "Utf8"
)

libraryDependencies ++= Seq(
	"com.typesafe.play" %% "play" % "2.4.3",
	"org.specs2" %% "specs2-core" % "3.6.4" % "test"
)

publishMavenStyle := true

publishTo := {
	val nexus = "https://oss.sonatype.org/"
	if (isSnapshot.value)
		Some("snapshots" at nexus + "content/repositories/snapshots")
	else
		Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
