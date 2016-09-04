package manual

import cats.{Functor, MonadReader}
import cats.data.{OptionT, Reader}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by denis on 8/1/16.
  */


case class FutureOption[A](internal: Future[Option[A]]) {
  def map[B](f: A => B): FutureOption[B] = FutureOption {
    internal.map(_.map(f))
  }
  def flatMap[B](f: A => FutureOption[B]): FutureOption[B] = {
    FutureOption {
      internal.flatMap(maybeValue => {
        maybeValue match {
          case Some(value) => f(value).internal
          case None => Future.successful(None)
        }
      })
    }
  }
}


object OptionTransformers {

  def main(args: Array[String]) {

    def generateNum: Future[Option[Long]] = Future {
      val source = Math.round(Math.random * 100)
      if (source <= 60) Some(source) else None
    }

    val maybeNum1F = generateNum
    val maybeNum2F = generateNum

/*
    val resultFO = for {
      num1 <- FutureOption(maybeNum1F)
      num2 <- FutureOption(maybeNum2F)
    } yield num1 + num2

    resultFO.internal.foreach( vv =>
      println(vv)
    )*/

    import cats.instances.future._

    val resultFO = for {
      num1 <- OptionT(maybeNum1F)
      num2 <- OptionT(maybeNum2F)
    } yield {
      num1 + num2
    }

    resultFO.value.foreach( vv =>
      println(vv)
    )



    Thread.sleep(1000)


  }
}
