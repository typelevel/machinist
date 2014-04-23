name := "machinist"

organization := "org.typelevel"

version := "0.2.2"

scalaVersion := "2.10.4"

crossScalaVersions := Seq("2.11.0")

seq(bintrayResolverSettings: _*)

libraryDependencies <++= (scalaVersion) { v =>
  Seq(
    "org.scala-lang" % "scala-compiler" % v % "provided",
    "org.scala-lang" % "scala-reflect" % v
  )
}

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked"
)

seq(bintrayPublishSettings: _*)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
