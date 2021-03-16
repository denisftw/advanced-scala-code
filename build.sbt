name := """advanced-scala-code"""

val monocleVersion = "1.5.1-cats"
val ioIterateeVersion = "0.18.0"
val fs2Version = "1.0.0"
val circeVersion = "0.10.0"
val finchVersion = "0.25.0"
val http4sVersion = "0.20.0-M2"
val doobieVersion = "0.6.0"
val monixVersion = "3.0.0-RC2"
val catsVersion = "1.4.0"
val catsEffectVersion = "1.0.0"
val scalazVersion = "7.2.27"
val ahcVersion = "2.6.0"
val akkaVersion = "2.5.17"
val shapelessVersion = "2.3.3"
val spireVersion = "0.16.0"
val simulacrumVersion = "0.14.0"
val scalaTagsVersion = "0.6.7"
val logbackVersion = "1.2.3"
val macroParadiseVersion = "2.1.0"

val commonSettings = Seq(
  scalaVersion := "2.12.7",
  organization := "com.appliedscala",
  version := "1.0-SNAPSHOT"
)

val macroParadiseSettings = Seq(
  addCompilerPlugin("org.scalamacros" % "paradise" % macroParadiseVersion cross CrossVersion.full)
)


val logbackClassicDep = "ch.qos.logback" % "logback-classic" % logbackVersion

val catsDependencies = Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-free" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion
)

val circeDependencies = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % circeVersion)

val iterateeDependencies = Seq(
  "io.iteratee" %% "iteratee-core" % ioIterateeVersion,
  "io.iteratee" %% "iteratee-monix" % ioIterateeVersion,
  "io.iteratee" %% "iteratee-files" % ioIterateeVersion
)

val fs2Dependencies = Seq(
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version
)

val scalazDependencies = Seq(
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
)

val finchDependencies = Seq(
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion
)

val http4sDependencies = Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  logbackClassicDep
)

val scalaTagsDependencies = Seq(
  "com.lihaoyi" %% "scalatags" % scalaTagsVersion
)

val typeclassesDependencies = Seq(
  "com.github.mpilquist" %% "simulacrum" % simulacrumVersion
)

val monixDependencies = Seq(
  "io.monix" %% "monix" % monixVersion
)

val ahcDependencies = Seq(
  "org.asynchttpclient" % "async-http-client" % ahcVersion,
  logbackClassicDep
)

val monocleDependencies = Seq(
  "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
  "org.typelevel" %% "spire" % spireVersion
)

val shapelessDependencies = Seq(
  "com.chuusai" %% "shapeless" % shapelessVersion
)

val doobieDependencies = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  logbackClassicDep
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

lazy val finch = (project in file("finch")).settings(commonSettings).settings(libraryDependencies ++= (finchDependencies ++ scalaTagsDependencies ++ ahcDependencies))

lazy val http4s = (project in file("http4s")).settings(commonSettings).settings(libraryDependencies ++= (http4sDependencies ++ scalaTagsDependencies ++ ahcDependencies))

lazy val scalaz = (project in file("scalaz")).settings(commonSettings).settings(libraryDependencies ++= (scalazDependencies ++ ahcDependencies)).dependsOn(base)

lazy val doobie = (project in file("doobie")).settings(commonSettings).settings(libraryDependencies ++= doobieDependencies)

lazy val monix = (project in file("monix")).settings(commonSettings).settings(libraryDependencies ++= (monixDependencies ++ ahcDependencies)).dependsOn(base)

lazy val misc = (project in file("misc")).settings(commonSettings)

lazy val base = (project in file("base")).settings(commonSettings)
