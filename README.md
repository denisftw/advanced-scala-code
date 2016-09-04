# Mastering Advanced Scala

This is the companion code repository for the [Mastering Advanced Scala](https://leanpub.com/mastering-advanced-scala) book published by Leanpub.

## Description

The code is organized into several subprojects. Each subproject corresponds to one specific chapter in the book and contains source files of featured standalone examples:

|subproject|featured libraries|chapter|
|--------|-------------|------|
|`typeclasses`|[Simulacrum](https://github.com/mpilquist/simulacrum)|Advanced language features|
|`scalaz`|[ScalaZ](https://github.com/scalaz/scalaz)|Exploring ScalaZ|
|`cats`|[Cats](https://github.com/typelevel/cats)|Exploring Cats|
|`iteratees`|[iteratee.io](https://github.com/travisbrown/iteratee)|Understanding iteratees|
|`monix`|[Monix](https://github.com/monixio/monix)|Working with asynchronous code|
|`monocle`|[Monocle](https://github.com/julien-truffaut/Monocle/)|Lenses and other optics|
|`fs2`|[FS2](https://github.com/functional-streams-for-scala/fs2)|Stream processing|
|`circe`|[Circe](https://github.com/travisbrown/circe)|Working with JSON|
|`shapeless`|[Shapeless](https://github.com/milessabin/shapeless)|Generic programming with Shapeless|
|`finch`|[Finch](https://github.com/finagle/finch)|Purely functional HTTP services|
|`htt4s`|[http4s](https://github.com/http4s/http4s)|Purely functional HTTP services|
|`doobie`|[doobie](https://github.com/tpolecat/doobie)|Database access with doobie|

All artifacts are fetched by [sbt-coursier](https://github.com/alexarchambault/coursier), which is a great project in its own right. Also, we're using [Spire](https://github.com/non/spire) in some examples that require precise arithmetic.
