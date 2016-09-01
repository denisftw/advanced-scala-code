


/**
  * Created by denis on 8/16/16.
  */
object FinchMain {

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

  def main(args: Array[String]) {

    import io.finch._

    // index
    val index: Endpoint0 = /
    val indexE: Endpoint[String] = index.apply { Ok("Hello World!") }

    // time
    val timeE = get("time") { Ok(System.currentTimeMillis().toString) }

    // hello/:username?id=XXX
    val greetE = get("greet" ::
        string.shouldNot("be less than two letters"){_.length < 2} ::
        param("id").as[Int].should("be more than zero"){_ > 0}) {
      (userName: String, id: Int) =>
        if (id % 2 == 0) {
          BadRequest(new Exception("ID is wrong!"))
        } else {
          Ok(s"Hello, $userName! Your number is #$id")
        }
    }

    // data/users/:id
    import io.finch.circe._
    import io.circe.generic.auto._

    case class PersonInfo(id: Long, firstName: String, lastName: String, age: Int)

    val personInfoE = get("data" :: "users" :: long("id")) { (id: Long) =>
      Ok(PersonInfo(id, "Joe", "Black", 42))
    }

    // doc
    val docE: Endpoint[Response] = get("doc" :: string("name")).map { (name: String) =>
      val document = HtmlTemplates.hello(name)
      htmlResponse(document)
    }

    {
      import shapeless._

      type StringOrInt = String :+: Int :+: CNil
    }

    val api = indexE :+: greetE :+: timeE :+: personInfoE :+: docE

    import com.twitter.finagle.Http
    import com.twitter.util.Await
    val server = Http.server.serve(":8080", api.toService)
    Await.ready(server)
  }
}
