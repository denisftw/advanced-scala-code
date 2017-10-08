package manual

import cats.{Functor, Monad}
import cats.data.{EitherT}

import scala.concurrent.Future
import scalaz.concurrent.Task

/**
  * Created by denis on 8/1/16.
  */
class GenerationException(number: Long, message: String)
  extends Exception(message)

object NumberProducer {
  import cats.syntax.either._
  def queryNextNumber: Task[Either[GenerationException, Long]] = Task {
    Either.catchOnly[GenerationException] {
      val source = Math.round(Math.random * 100)
      if (source <= 80) source
      else throw new GenerationException(source, "The generated number is too big!")
    }
  }
}


object EitherTransformers {
  def main(args: Array[String]): Unit = {

    val num1TX = NumberProducer.queryNextNumber
    val num2TX = NumberProducer.queryNextNumber
/*
    val resultTX = for {
      num1X <- num1TX
      num2X <- num2TX
    } yield {
      for {
        num1 <- num1X
        num2 <- num2X
      } yield num1 + num2
    }

    val resultX = resultTX.unsafePerformSync
    println(s"Result: $resultX")*/
/*
    implicit val taskFunctor = new Functor[Task] {
      override def map[A, B](fa: Task[A])(f: (A) => B): Task[B] = fa.map(f)
    }*/
    implicit val taskMonad = new Monad[Task] {
      def tailRecM[A,B](a: A)(f: A => Task[Either[A,B]]): Task[B] =
        Task.suspend(f(a)).flatMap {
          case Left(continueA) => tailRecM(continueA)(f)
          case Right(b) => Task.now(b)
        }
      override def flatMap[A, B](fa: Task[A])(f: (A) => Task[B]): Task[B] = fa.flatMap(f)
      override def pure[A](x: A): Task[A] = Task.now(x)
    }

    val resultTXT = for {
      num1 <- EitherT(num1TX)
      num2 <- EitherT(num2TX)
    } yield num1 + num2

    val resultX = resultTXT.value.unsafePerformSync
    println(s"Result: $resultX")

  }
}
