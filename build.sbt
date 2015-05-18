name := "machinist root project"

crossScalaVersions := Seq("2.10.5", "2.11.6")

lazy val root = project.in(file(".")).
  aggregate(machinistJS, machinistJVM).
  settings(
      publish := {},
      publishLocal := {},
      sources in Compile := Seq(),
      sources in Test := Seq()
  )

lazy val machinist = crossProject.
  crossType(CrossType.Pure).
  in(file(".")).
  settings(bintrayResolverSettings ++ bintrayPublishSettings ++ Seq(
      name := "machinist",
      organization := "org.typelevel",
      version := "0.3.1-SNAPSHOT",
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
      }
  ):_*).
  jvmSettings(
      // Add JVM-specific settings here
  ).
  jsSettings(
      // Add JS-specific settings here
  )

lazy val machinistJVM = machinist.jvm
lazy val machinistJS = machinist.js
