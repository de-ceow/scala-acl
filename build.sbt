name := "playacl"

version := "0.7.0"

organization := "com.github.scyks"

scalaVersion := "2.11.8"

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
		<url>git@github.com:scyks/play-acl.git</url>
		<connection>scm:git@github.com:scyks/play-acl.git</connection>
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
	specs2 % Test,
	"com.typesafe.play" %% "play" % "2.5.0",
	"com.typesafe.play" %% "play-test" % "2.5.0" % Test,
	"org.slf4j" % "slf4j-simple" % "1.7.2" % Test
)

publishMavenStyle := true

publishTo := {
	val nexus = "https://oss.sonatype.org/"
	if (isSnapshot.value)
		Some("snapshots" at nexus + "content/repositories/snapshots")
	else
		Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
