import scala.util.control.TailCalls._
import scala.annotation.tailrec
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object StackSafetyDemo {

  def sum(xs: List[Int]): Int = {
    def loopNaive(list: List[Int]): Int = {
      list match {
        case head :: tail => head + loopNaive(tail)
        case Nil          => 0
      }
    }

    def loopTailCall(list: List[Int]): TailRec[Int] = {
      list match {
        case head :: tail => tailcall(loopTailCall(tail).map(head + _))
        case Nil          => done(0)
      }
    }

    def loopFutureNaive(list: List[Int]): Future[Int] = {
      list match {
        case head :: tail => loopFutureNaive(tail).map(_ + head)
        case Nil          => Future.successful(0)
      }
    }

    def loopFuture(list: List[Int]): Future[Int] = {
      list match {
        case head :: tail =>
          Future.unit.flatMap { _ =>
            loopFuture(tail).map(_ + head)
          }
        case Nil => Future.successful(0)
      }
    }

    @tailrec
    def loopTailRec(list: List[Int], acc: Int): Int = {
      list match {
        case head :: tail => loopTailRec(tail, acc + head)
        case Nil          => acc
      }
    }

    Await.result(loopFutureNaive(xs), Duration.Inf)
  }

  def main(args: Array[String]): Unit = {
    val xs = 1.to(10000).toList
    println(sum(xs))
  }
}
