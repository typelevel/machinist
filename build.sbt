import ReleaseTransformations._

name := "machinist root project"

crossScalaVersions := Seq("2.10.5", "2.11.7")

lazy val root = project.in(file(".")).
  aggregate(machinistJS, machinistJVM).
  settings(
    publish := {},
    publishLocal := {},
    sources in Compile := Seq(),
    sources in Test := Seq())

lazy val machinist = crossProject
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(
    name := "machinist",
    organization := "org.typelevel",
    scalaVersion := "2.11.6",
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-unchecked"
    ),
    libraryDependencies <++= (scalaVersion) { v =>
      Seq(
        "org.scala-lang" % "scala-compiler" % v % "provided",
        "org.scala-lang" % "scala-reflect" % v
      )
    })
  .settings(publishSettings: _*)
  .jvmSettings( /* Add JVM-specific settings here */)
  .jsSettings( /* Add JS-specific settings here */)

lazy val machinistJVM = machinist.jvm
lazy val machinistJS = machinist.js

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),

  publishTo <<= (version).apply { v =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },

  pomExtra := (
    <scm>
      <url>git@github.com:non/jawn.git</url>
      <connection>scm:git:git@github.com:non/jawn.git</connection>
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
    ReleaseStep(action = Command.process("publishSigned", _)),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges))
