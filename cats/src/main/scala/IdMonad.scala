import cats.{Functor, Monad}

import scala.annotation.tailrec

/**
  * Created by denis on 8/3/16.
  */
object IdMonad {

  type Id[A] = A

  def main(args: Array[String]) {

    val greet = (name: String) => s"Hello $name"
    val greeting = Functor[Id].map("Joe")(greet)

    import cats.syntax.flatMap._
    import cats.syntax.functor._

    implicit val idMonad = new Monad[Id] {
      @tailrec override def tailRecM[A, B](a: A)(f: (A) => Id[Either[A, B]]):
        Id[B] = f(a) match {
        case Left(a1) => tailRecM(a1)(f)
        case Right(b) => b
      }
      override def pure[A](x: A): Id[A] = x
      override def flatMap[A, B](fa: Id[A])(f: (A) => Id[B]): Id[B] = f(fa)
    }

    val id1: Id[Int] = 42
    val id2: Id[Int] = 23

    val resultId = for {
      num1 <- id1
      num2 <- id2
    } yield num1 + num2

    println(resultId)
  }
}
