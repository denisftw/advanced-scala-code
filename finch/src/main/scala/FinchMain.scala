import io.finch._
import com.twitter.finagle.Http
import com.twitter.util.Await

/**
  * Created by denis on 8/16/16.
  */
object FinchMain {

  def main(args: Array[String]) {

    val helloEndpoint = get("hello").apply { Ok("Hello, World!") }

    val helloTime = get("time").apply { Ok(System.currentTimeMillis().toString) }

    val api = helloEndpoint | helloTime

    val server = Http.server.serve(":8080", api.toServiceAs[Text.Plain])

    Await.ready(server)
  }
}
