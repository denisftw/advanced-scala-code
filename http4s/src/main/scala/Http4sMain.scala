

import java.util.UUID
import scala.util.Try
object UuidVar {
  def unapply(s: String): Option[UUID] = {
    Try { UUID.fromString(s) }.toOption
  }
}

object Endpoints {
  import org.http4s._
  import org.http4s.dsl._

  val helloWorldService = HttpService {
    case GET -> Root / "hello" / IntVar(name) =>
      Ok(s"Hello $name")
  }

  val idService = HttpService {
    case GET -> Root / "id" / UuidVar(id) =>
      Ok(s"Your ID is $id")
  }

  val timeService = HttpService {
    case GET -> Root / "time" =>
      Ok(System.currentTimeMillis().toString)
  }
}


/**
  * Created by denis on 8/25/16.
  */
import org.http4s.server.ServerApp
object Http4sMain extends ServerApp {

  import scalaz.concurrent.Task
  import org.http4s.server.Server
  override def server(args: List[String]): Task[Server] = {

    import Endpoints._
    import org.http4s.server.syntax._
    val api = helloWorldService orElse timeService orElse idService

    import org.http4s.server.blaze._
    BlazeBuilder.bindHttp(8080, "localhost").
      mountService(api, "/").start
  }
}
