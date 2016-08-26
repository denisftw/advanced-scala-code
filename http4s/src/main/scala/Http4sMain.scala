


object Endpoints {
  import org.http4s._
  import org.http4s.dsl._

  val helloWorldService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello $name")
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
    val services = helloWorldService orElse timeService

    import org.http4s.server.blaze._
    BlazeBuilder.bindHttp(8080, "localhost").
      mountService(services, "/").start
  }
}
