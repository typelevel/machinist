import ReleaseTransformations._

lazy val machinistSettings = Seq(
  organization := "org.typelevel",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("http://github.com/typelevel/machinist")),

  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.10.6", "2.11.12", "2.12.4", "2.13.0-M2"),

  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked"
  ),

  libraryDependencies += { "org.scala-lang" % "scala-reflect" % scalaVersion.value },

  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },

  pomExtra := (
    <scm>
      <url>git@github.com:typelevel/machinist.git</url>
      <connection>scm:git:git@github.com:typelevel/machinist.git</connection>
    </scm>
    <developers>
      <developer>
        <id>d_m</id>
        <name>Erik Osheim</name>
        <url>http://github.com/non/</url>
      </developer>
      <developer>
        <id>tixxit</id>
        <name>Tom Switzer</name>
        <url>http://github.com/tixxit/</url>
      </developer>
    </developers>
  ),

  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges))

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false)

lazy val root = project
  .in(file("."))
  .aggregate(machinistJS, machinistJVM)
  .settings(name := "machinist-root")
  .settings(machinistSettings: _*)
  .settings(noPublish: _*)

lazy val machinist = crossProject
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(name := "machinist")
  .settings(machinistSettings: _*)

lazy val machinistJVM = machinist.jvm

lazy val machinistJS = machinist.js
