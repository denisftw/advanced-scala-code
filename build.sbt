name := """advanced-scala-code"""

val monocleVersion = "1.2.2"
val ioIterateeVersion = "0.6.0"
val fs2Version = "0.9.0-M6"
val circeVersion = "0.5.1"
val finchVersion = "0.11.0-M3"
val http4sVersion = "0.14.3"
val doobieVersion = "0.3.1-SNAPSHOT"
//val doobieVersion = "0.3.0"
val monixVersion = "2.0.0"
val catsVersion = "0.7.2"
val scalazVersion = "7.2.4"
val ahcVersion = "2.0.11"

val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.appliedscala",
  version := "1.0-SNAPSHOT",
  resolvers += Resolver.sonatypeRepo("snapshots"),
  resolvers += Resolver.sonatypeRepo("releases")
)

val macroParadiseSettings = Seq(
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)


val logbackClassicDep = "ch.qos.logback" % "logback-classic" % "1.1.7"

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)

val catsDependencies = Seq(
  "org.typelevel" %% "cats" % catsVersion,
  "org.typelevel" %% "cats-free" % catsVersion
)

val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % circeVersion)

val iterateeDependencies = Seq(
  "io.iteratee" %% "iteratee-core" % ioIterateeVersion,
  "io.iteratee" %% "iteratee-scalaz" % ioIterateeVersion,
  "io.iteratee" %% "iteratee-files" % ioIterateeVersion
)

val fs2Dependencies = Seq(
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version
)

val scalazDependencies = Seq(
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
  "com.typesafe.akka" %% "akka-actor" % "2.4.8"
)

val finchDependencies = Seq(
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion
)

val http4sDependencies = Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  logbackClassicDep
)

val scalaTagsDependencies = Seq(
  "com.lihaoyi" %% "scalatags" % "0.6.0"
)

val typeclassesDependencies = Seq(
  "com.github.mpilquist" %% "simulacrum" % "0.7.0"
)

val monixDependencies = Seq(
  "io.monix" %% "monix" % monixVersion,
  "io.monix" %% "monix-cats" % monixVersion
)

val ahcDependencies = Seq(
  "org.asynchttpclient" % "async-http-client" % ahcVersion,
  logbackClassicDep
)

val monocleDependencies = Seq(
  "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
  "org.spire-math" %% "spire" % "0.11.0"
)

val shapelessDependencies = Seq(
  "com.chuusai" %% "shapeless" % "2.3.1"
)

val doobieDependencies = Seq(
  "org.tpolecat" %% "doobie-core-cats" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres-cats" % doobieVersion
)


lazy val root = (project in file(".")).settings(commonSettings).aggregate(
  typeclasses, cats, iteratees, monocle, fs2, circe, shapeless, finch, scalaz, http4s, monix, base, doobie)

lazy val typeclasses = (project in file("typeclasses")).settings(commonSettings ++ macroParadiseSettings).settings(libraryDependencies ++= typeclassesDependencies)

lazy val cats = (project in file("cats")).settings(commonSettings).settings(libraryDependencies ++= (catsDependencies ++ scalazDependencies))

lazy val iteratees = (project in file("iteratees")).settings(commonSettings).settings(libraryDependencies ++= iterateeDependencies)

lazy val monocle = (project in file("monocle")).settings(commonSettings).settings(libraryDependencies ++= (monocleDependencies ++ catsDependencies))

lazy val fs2 = (project in file("fs2")).settings(commonSettings).settings(libraryDependencies ++= fs2Dependencies)

lazy val circe = (project in file("circe")).settings(commonSettings).settings(libraryDependencies ++= (circeDependencies ++ fs2Dependencies))

lazy val shapeless = (project in file("shapeless")).settings(commonSettings).settings(libraryDependencies ++= shapelessDependencies)

lazy val finch = (project in file("finch")).settings(commonSettings).settings(libraryDependencies ++= (finchDependencies ++ scalaTagsDependencies))

lazy val http4s = (project in file("http4s")).settings(commonSettings).settings(libraryDependencies ++= (http4sDependencies ++ scalaTagsDependencies))

lazy val scalaz = (project in file("scalaz")).settings(commonSettings).settings(libraryDependencies ++= (scalazDependencies ++ ahcDependencies)).dependsOn(base)

lazy val doobie = (project in file("doobie")).settings(commonSettings).settings(libraryDependencies ++= doobieDependencies)

lazy val monix = (project in file("monix")).settings(commonSettings).settings(libraryDependencies ++= (monixDependencies ++ ahcDependencies)).dependsOn(base)

lazy val base = (project in file("base")).settings(commonSettings)
