name := "machinist"

organization := "org.typelevel"

version := "0.2.0"

scalaVersion := "2.10.3"

seq(bintrayResolverSettings: _*)

libraryDependencies <++= (scalaVersion) {
  v => Seq("org.scala-lang" % "scala-compiler" % v % "provided")
}

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked"
)

seq(bintrayPublishSettings: _*)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
