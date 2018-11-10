import java.util.concurrent.Executors

import cats.effect.IO

import scala.concurrent.ExecutionContext

object PrintThread {
  def execute[A](block: => A): A = {
    print(s"[${Thread.currentThread().getName}] ")
    block
  }
}

object CatsEffect {
  def main(args: Array[String]): Unit = {
    val blockingService = Executors.newCachedThreadPool()
    val blockingCtx   = ExecutionContext.fromExecutor(blockingService)
    val global = ExecutionContext.global
    implicit val contextShift = IO.contextShift(global)

    val ioa: IO[Unit] = for {
      _ <- contextShift.shift
      _ <- IO { PrintThread.execute(println("Enter your name: ")) }
      name <- contextShift.evalOn(blockingCtx)(
        IO{ PrintThread.execute(scala.io.StdIn.readLine()) }
      )
      _ <- IO { PrintThread.execute(println(s"Hello $name!")) }
      _ <- IO { PrintThread.execute(blockingService.shutdown()) }
    } yield ()

    ioa.unsafeRunSync()
  }
}
