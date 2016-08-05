package manual

import cats.{Functor, Monad}
import cats.data.{Xor, XorT}

import scala.concurrent.Future
import scalaz.concurrent.Task

/**
  * Created by denis on 8/1/16.
  */
class GenerationException(number: Long, message: String)
  extends Exception(message)

object NumberProducer {
  def queryNextNumber: Task[Xor[GenerationException, Long]] = Task {
    Xor.catchOnly[GenerationException] {
      val source = Math.round(Math.random * 100)
      if (source <= 80) source
      else throw new GenerationException(source, "The generated number is too big!")
    }
  }
}


object XorTransformers {
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
      override def flatMap[A, B](fa: Task[A])(f: (A) => Task[B]): Task[B] = fa.flatMap(f)
      override def pure[A](x: A): Task[A] = Task.now(x)
    }

    val resultTXT = for {
      num1 <- XorT(num1TX)
      num2 <- XorT(num2TX)
    } yield num1 + num2

    val resultX = resultTXT.value.unsafePerformSync
    println(s"Result: $resultX")

  }
}
