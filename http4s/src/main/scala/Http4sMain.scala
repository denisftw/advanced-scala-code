import java.util.UUID

import cats.effect.IO
import scala.util.Try

case class Person(name: String, age: Int)

object Endpoints {
  import org.http4s._
  import org.http4s.dsl.io._

  val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / IntVar(number) =>
      Ok(s"Hello, your number is $number")
  }

  val asyncRequest = HttpRoutes.of[IO] {
    case GET -> Root / "async" =>
      Ok {
        IO.async[String] { eitherCb =>
          import org.asynchttpclient.Dsl._
          val whenResponse = asyncHttpClient.
            prepareGet("https://httpbin.org/get").execute()
          whenResponse.toCompletableFuture.whenComplete((res, th) => {
            if (th != null) {
              eitherCb(Left(th))
            } else eitherCb(Right(res.getResponseBody))
          })
        }
      }
  }

  val jsonRequest = HttpRoutes.of[IO] {
    case GET -> Root / "json" =>
      import org.http4s.circe._         // EntityEncoder[IO, Json]
      import io.circe.generic.auto._    // automatic codecs for Person
      import io.circe.syntax._          // asJson method
      Ok {
        Person("Joe", 42).asJson
      }
  }

  val idService = HttpRoutes.of[IO] {
    case GET -> Root / "id" / UuidVar(id) =>
      Ok(s"Your ID is $id")
  }

  val timeService = HttpRoutes.of[IO] {
    case GET -> Root / "time" =>
      Ok(System.currentTimeMillis().toString)
  }

  object UuidVar {
    def unapply(s: String): Option[UUID] = {
      Try { UUID.fromString(s) }.toOption
    }
  }
}


import cats.effect.{ExitCode, IO, IOApp}
object Http4sMain extends IOApp {

  import Endpoints._
  import cats.implicits._
  import org.http4s.implicits._
  import org.http4s.server.blaze._
  import org.http4s.server.Router

  val api = helloWorldService <+> timeService <+> idService <+> asyncRequest <+> jsonRequest

  val httpApp = Router("/" -> api).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080)
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
