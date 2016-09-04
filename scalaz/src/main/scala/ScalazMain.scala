import java.nio.charset.Charset
import java.util.concurrent.{ExecutorService, Executors}

import org.asynchttpclient.{AsyncCompletionHandler, DefaultAsyncHttpClient, Response}

import scala.concurrent.Future
import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.Task

/**
  * Created by denis on 7/24/16.
  */
object ScalazMain {

  def main(args: Array[String]): Unit = {

    def performAction(num: Int): Unit = println(s"Task #$num is executing in ${Thread.currentThread().getName}")

    import scala.concurrent.ExecutionContext.Implicits.global
    val result1F = Future {
      performAction(0)
    }

    val result2F = Future.successful {
      performAction(1)
    }

    // Executes immediately in the main thread
    val result2T = Task.now {
      performAction(2)
    }

    // Schedules an execution in the main thread
    val result3T = Task {
      performAction(3)
      throw new Exception("!!!")
    }


    // Schedules an execution in a default worker thread
    // = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime.availableProcessors), DefaultDaemonThreadFactory)
    val result4T = Task {
      performAction(4)
    }

    result4T.unsafePerformAsync(_ => ())

    implicit val executorService = Executors.newSingleThreadExecutor()
    val result5T = Task {
      performAction(5)
    }
    result4T.unsafePerformSync

    val asyncHttpClient = new DefaultAsyncHttpClient()
    arm.ArmUtils.using(asyncHttpClient) {
      val result6T = Task.async[String](handler => {
        asyncHttpClient.prepareGet("https://httpbin.org/get").execute(new AsyncCompletionHandler[Response]() {
          override def onCompleted(response: Response): Response = {
            handler(\/.right(response.getResponseBody(Charset.forName("UTF-8"))))
            response
          }
          override def onThrowable(t: Throwable): Unit = {
            handler(-\/(t))
          }
        })
      })
      val responseString = result6T.unsafePerformSync
      println(responseString)
    }

  }
}
