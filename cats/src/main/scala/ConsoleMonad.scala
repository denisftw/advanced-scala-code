import cats.Monad

import scala.io.StdIn

/** Created by denis on 8/11/16.
  */
trait ConsoleAction[A] {
  def bind[B](f: A => ConsoleAction[B]): ConsoleAction[B]
}

case class ReadFromConsole() extends ConsoleAction[String] {
  override def bind[B](f: (String) => ConsoleAction[B]): ConsoleAction[B] = {
    val input = StdIn.readLine()
    f(input)
  }
}

case class WriteToConsole(output: String) extends ConsoleAction[Unit] {
  override def bind[B](f: (Unit) => ConsoleAction[B]): ConsoleAction[B] = {
    println(output)
    f()
  }
}

case class NopConsole() extends ConsoleAction[Unit] {
  override def bind[B](f: (Unit) => ConsoleAction[B]): ConsoleAction[B] = {
    f()
  }
}

object ConsoleMonad {
  def main(args: Array[String]) {
    implicit val consoleMonad = new Monad[ConsoleAction] {
      override def flatMap[A, B](fa: ConsoleAction[A])(f: (A) => ConsoleAction[B]): ConsoleAction[B] = {
        fa.bind(f)
      }
      override def pure[A](x: A): ConsoleAction[A] = {
        NopConsole().asInstanceOf[ConsoleAction[A]]
      }
      // Not stack-safe
      override def tailRecM[A, B](a: A)(f: (A) => ConsoleAction[Either[A, B]]): ConsoleAction[B] = {
        flatMap(f(a)) {
          case Left(next) => tailRecM(next)(f)
          case Right(b)   => pure(b)
        }
      }
    }

    import cats.syntax.flatMap._
    import cats.syntax.functor._

    for {
      _ <- WriteToConsole("Write your name: ")
      name <- ReadFromConsole()
      _ <- WriteToConsole(s"Hello $name")
    } yield ()
  }
}
