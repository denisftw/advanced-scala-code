import scala.io.StdIn
import scalaz.concurrent.Task

/**
  * Created by denis on 8/11/16.
  */
trait ActionA[A]
case class ReadAction() extends ActionA[String]
case class WriteAction(output: String) extends ActionA[Unit]

object FreeMonad {
  def main(args: Array[String]) {

    import cats.free.Free
    type ActionF[A] = Free[ActionA, A]

    import cats.free.Free.liftF
    def read(): ActionF[String] = liftF[ActionA, String](ReadAction())
    def write(output: String): ActionF[Unit] = liftF[ActionA, Unit](WriteAction(output))

    val result = for {
      _ <- write("Write your name: ")
      name <- read()
      res <- write(s"Hello $name")
    } yield res
    // result: Free[ActionA, Unit]

    import cats.arrow.NaturalTransformation
    import cats.{Id, Monad}
    /*
    val idInterpreter: NaturalTransformation[ActionA, Id] =
      new NaturalTransformation[ActionA, Id] {
      override def apply[A](fa: ActionA[A]): Id[A] = fa match {
        case ReadAction() =>
          val input = StdIn.readLine()
          input
        case WriteAction(output) =>
          println(output)
      }
    }*/

    def taskInterpreter: NaturalTransformation[ActionA, Task] =
      new NaturalTransformation[ActionA, Task] {
      def apply[A](fa: ActionA[A]): Task[A] = (fa match {
        case ReadAction() => Task.delay {
          val input = StdIn.readLine()
          input
        }
        case WriteAction(output) => Task.delay {
          println(output)
        }
      }).asInstanceOf[Task[A]]
    }

    implicit val taskMonad = new Monad[Task] {
      override def flatMap[A, B](fa: Task[A])(f: (A) => Task[B]):
        Task[B] = fa.flatMap(f)
      override def pure[A](x: A): Task[A] = Task.now(x)
    }

    result.foldMap(taskInterpreter).unsafePerformSync

  }
}
