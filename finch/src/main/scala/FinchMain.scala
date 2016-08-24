


/**
  * Created by denis on 8/16/16.
  */
object FinchMain {

  def main(args: Array[String]) {

    import io.finch._

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
    import com.twitter.finagle.http.Response
    import com.twitter.io.Buf
    val docE: Endpoint[Response] = get("doc").map { _ =>
      val rep = Response()
      val document = scalatex.Hello().render
      rep.content = Buf.Utf8(document)
      rep.contentType = "text/html"
      rep
    }

    val api = greetE :+: timeE :+: personInfoE :+: docE

    import com.twitter.finagle.Http
    import com.twitter.util.Await
    val server = Http.server.serve(":8080", api.toServiceAs[Application.Json])
    Await.ready(server)
  }
}
