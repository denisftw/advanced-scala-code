

/**
  * Created by denis on 9/2/16.
  */
object MonixTaskMain {

  def main(args: Array[String]): Unit = {

    import org.asynchttpclient.DefaultAsyncHttpClient
    val asyncHttpClient = new DefaultAsyncHttpClient()

    arm.ArmUtils.using(asyncHttpClient) {

      import org.asynchttpclient.Response
      import java.nio.charset.Charset
      import monix.eval.Task
      val result6T = Task.create[String]( (scheduler, callback) => {

        val lf = asyncHttpClient.prepareGet("https://httpbin.org/get").execute
        val javaFuture = lf.toCompletableFuture

        import java.util.function.BiConsumer
        javaFuture.whenComplete(new BiConsumer[Response, Throwable] {
          override def accept(response: Response, exc: Throwable): Unit = {
            if (exc == null) {
              callback.onSuccess(response.getResponseBody(Charset.forName("UTF-8")))
            } else callback.onError(exc)
          }
        })

        import monix.execution.Cancelable
        Cancelable.apply { () =>
          javaFuture.cancel(true)
        }
      })

      import monix.execution.Scheduler.Implicits.global
      /*val resultCF = result6T.runAsync

      import scala.concurrent.Await
      import scala.concurrent.duration._
      val result = Await.result(resultCF, 5.seconds)
      println(result)*/
    }
  }
}
