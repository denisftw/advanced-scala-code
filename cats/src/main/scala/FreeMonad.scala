import cats.Monad

import scala.io.StdIn
import scalaz.concurrent.Task

/**
  * Created by denis on 8/11/16.
  */
trait ActionA[A]
case class ReadActionA() extends ActionA[String]
case class WriteActionA(output: String) extends ActionA[Unit]

object FreeMonad {
  def main(args: Array[String]) {

    import cats.free.Free
    type ActionF[A] = Free[ActionA, A]

    import cats.free.Free.liftF
    def read(): ActionF[String] = liftF[ActionA, String](ReadActionA())
    def write(output: String): ActionF[Unit] = liftF[ActionA, Unit](WriteActionA(output))

    val result = for {
      _ <- write("Write your name: ")
      name <- read()
      res <- write(s"Hello $name")
    } yield res
    // result: Free[ActionA, Unit]

    import cats.~>
    def taskInterpreter: ActionA ~> Task =
      new (ActionA ~> Task) {
        def apply[A](fa: ActionA[A]): Task[A] = fa match {
          case ReadActionA() => Task.delay {
            val input = StdIn.readLine()
            input
          }
          case WriteActionA(output) => Task.delay {
            println(output)
          }
        }
      }

    implicit val taskMonad = new Monad[Task] {
      override def flatMap[A, B](fa: Task[A])(f: (A) => Task[B]): Task[B] = fa.flatMap(f)
      override def pure[A](x: A): Task[A] = Task.now(x)
    }

    result.foldMap(taskInterpreter).unsafePerformSync

  }
}
