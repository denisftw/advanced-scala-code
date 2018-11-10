

/**
  * Created by denis on 9/2/16.
  */
object MonixTaskMain {

  def main(args: Array[String]): Unit = {
    import org.asynchttpclient.DefaultAsyncHttpClient
    val asyncHttpClient = new DefaultAsyncHttpClient()
    arm.ArmUtils.using(asyncHttpClient) {
      import java.nio.charset.Charset
      import monix.eval.Task
      val result6T = Task.create[String]( (_, callback) => {
        val lf = asyncHttpClient.prepareGet("https://httpbin.org/get").execute()
        val javaFuture = lf.toCompletableFuture

        javaFuture.whenComplete { (response, exc) => {
          if (exc == null) {
            callback.onSuccess(response.getResponseBody(Charset.forName("UTF-8")))
          } else callback.onError(exc)
        }}

        import monix.execution.Cancelable
        Cancelable.apply { () =>
          javaFuture.cancel(true)
        }
      })

      import monix.execution.Scheduler.Implicits.global
      val resultCF = result6T.runToFuture

      import scala.concurrent.Await
      import scala.concurrent.duration._
      val result = Await.result(resultCF, 5.seconds)
      println(result)
    }
  }
}
