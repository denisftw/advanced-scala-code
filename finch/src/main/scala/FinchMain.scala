import java.util.UUID

import io.finch._
import io.finch.syntax.get

case class Person(name: String, age: Int)
case class PersonInfo(id: Long, firstName: String, lastName: String, age: Int)

object HtmlEndpoints {
  import com.twitter.finagle.http.Response
  import com.twitter.io.Buf

  private def htmlResponse(document: String): Response = {
    val rep = Response()
    rep.content = Buf.Utf8(document)
    rep.contentType = "text/html"
    rep
  }

  object HtmlTemplates {
    import scalatags.Text.all._

    def hello(name: String): String = {
      html(
        body(
          h1(s"Hello ${name.capitalize}!"),
          div(
            p("Welcome to the show!")
          )
        )
      )
    }.toString()
  }

  // http://localhost:8080/doc/joe
  val docE: Endpoint[Response] = get("doc" :: path[String]) { name: String =>
    val document = HtmlTemplates.hello(name)
    htmlResponse(document)
  }
}

object Endpoints {
  import io.finch._
  import io.finch.syntax._

  // async (Note: code is incorrecly highlighted as error in IntelliJ)
  val asyncE = get("async" :: path[Int]).mapAsync { index: Int =>
    import com.twitter.util.FuturePool
    import com.twitter.util.Promise
    import io.circe.Json
    import io.circe.parser._
    FuturePool.unboundedPool {
      import org.asynchttpclient.Dsl._
      val whenResponse = asyncHttpClient.
        prepareGet(s"https://jsonplaceholder.typicode.com/todos/$index").execute()
      val promise = Promise[Json]()
      whenResponse.toCompletableFuture.whenComplete((response, throwable) => {
        if (throwable != null) {
          promise.setException(throwable)
        } else {
          val body = response.getResponseBody
          promise.setValue(parse(body).getOrElse(Json.Null))
        }
      })
      promise
    }.flatten
  }

  // time
  val timeE = get("time") { Ok(System.currentTimeMillis().toString) }

  // http://localhost:8080/greet/joe?id=33
  val greetE: Endpoint[String] = get("greet" ::
    path[String].shouldNot("be less than two letters"){_.length < 2} ::
    param[Int]("id").should("be more than zero"){_ > 0}) {
    (userName: String, id: Int) =>
      if (id % 2 == 0) {
        BadRequest(new Exception("ID is wrong!"))
      } else {
        Ok(s"Hello, ${userName.capitalize}! Your number is #$id")
      }
  }

  // JSON - http://localhost:8080/data/users?id=33
  val personInfoE = get("data" :: "users" :: param[Long]("id")) { id: Long =>
    Ok(PersonInfo(id, "Joe", "Black", 42))
  }

  implicit val uuidDecoder: DecodeEntity[UUID] =
    DecodeEntity.instance { s =>
      com.twitter.util.Try(UUID.fromString(s))
    }

  val idE: Endpoint[String] = get("id" :: path[UUID]) { id: UUID =>
    Ok(s"Your UUID has variant ${id.variant()}")
  }
}

object FinchMain {
  import com.twitter.finagle.Http
  import com.twitter.util.Await
  import Endpoints._
  import HtmlEndpoints._

  def main(args: Array[String]) {
    import io.circe.generic.auto._
    import io.finch.circe._
    val api = greetE :+: timeE :+: personInfoE :+: docE :+: idE :+: asyncE
    val server = Http.server.serve(":8080", api.toService)
    Await.ready(server)
  }
}
