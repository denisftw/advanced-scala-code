name := """applied-functional-scala"""

val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.appliedscala",
  version := "1.0-SNAPSHOT"
)

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)

val catsDependencies = Seq(
  "org.typelevel" %% "cats" % "0.6.1"
)

val scalazDependencies = Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.4",
  "org.scalaz" %% "scalaz-concurrent" % "7.2.4",
  "org.asynchttpclient" % "async-http-client" % "2.0.11"
)

lazy val root = (project in file(".")).settings(commonSettings).aggregate(typeclasses, cats)

lazy val typeclasses = (project in file("typeclasses")).settings(commonSettings)

lazy val cats = (project in file("cats")).settings(commonSettings).settings(libraryDependencies ++= catsDependencies)

lazy val scalaz = (project in file("scalaz")).settings(commonSettings).settings(libraryDependencies ++= scalazDependencies)