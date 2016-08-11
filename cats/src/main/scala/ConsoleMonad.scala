import cats.Monad

import scala.io.StdIn

/**
  * Created by denis on 8/11/16.
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

object ConsoleMonad {
  def main(args: Array[String]) {
    implicit val consoleMonad = new Monad[ConsoleAction] {
      override def flatMap[A, B](fa: ConsoleAction[A])(f: (A) => ConsoleAction[B]):
        ConsoleAction[B] = fa.bind(f)
      override def pure[A](x: A): ConsoleAction[A] =
        ReadFromConsole().asInstanceOf[ConsoleAction[A]]
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
