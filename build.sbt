name := """applied-functional-scala"""

val monocleVersion = "1.2.2"

val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.appliedscala",
  version := "1.0-SNAPSHOT",
  resolvers += Resolver.sonatypeRepo("snapshots"),
  resolvers += Resolver.sonatypeRepo("releases"),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)

val catsDependencies = Seq(
  "org.typelevel" %% "cats" % "0.6.1"
)

val iterateeDependencies = Seq(
  "io.iteratee" %% "iteratee-core" % "0.6.0-M1",
  "io.iteratee" %% "iteratee-scalaz" % "0.6.0-M1",
  "io.iteratee" %% "iteratee-files" % "0.6.0-M1"
)

val scalazDependencies = Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.4",
  "org.scalaz" %% "scalaz-concurrent" % "7.2.4",
  "org.asynchttpclient" % "async-http-client" % "2.0.11",
  "com.typesafe.akka" %% "akka-actor" % "2.4.8"
)

val typeclassesDependencies = Seq(
  "com.github.mpilquist" %% "simulacrum" % "0.7.0"
)

val monocleDependencies = Seq(
  "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-generic" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion
)

lazy val root = (project in file(".")).settings(commonSettings).aggregate(typeclasses, cats)

lazy val typeclasses = (project in file("typeclasses")).settings(commonSettings).settings(libraryDependencies ++= typeclassesDependencies)

lazy val cats = (project in file("cats")).settings(commonSettings).settings(libraryDependencies ++= catsDependencies)

lazy val iteratees = (project in file("iteratees")).settings(commonSettings).settings(libraryDependencies ++= iterateeDependencies)

lazy val monocle = (project in file("monocle")).settings(commonSettings).settings(libraryDependencies ++= monocleDependencies)

lazy val scalaz = (project in file("scalaz")).settings(commonSettings).settings(libraryDependencies ++= scalazDependencies)