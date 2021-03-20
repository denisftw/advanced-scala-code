import cats.{Monad => CatsMonad}

import scala.annotation.tailrec

object CatsMonadStackSafety {
  private implicit val catsMonadInstance: CatsMonad[Optional] = new CatsMonad[Optional] {
    override def flatMap[A, B](fa: Optional[A])(f: A => Optional[B]): Optional[B] = {
      fa.bind(f)
    }
    /*
    // Default implementation doesn't work for classes like `Optional`
    override def tailRecM[A, B](a: A)(f: A => Optional[Either[A, B]]): Optional[B] = {
      flatMap(f(a)) {
        case Left(next) => tailRecM(next)(f)
        case Right(b)   => pure(b)
      }
    }
     */
    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Optional[Either[A, B]]): Optional[B] = {
      f(a) match {
        case Absence              => Absence
        case Presence(Left(next)) => tailRecM(next)(f)
        case Presence(Right(b))   => pure(b)
      }
    }
    override def pure[A](x: A): Optional[A] = Presence(x)
  }

  import cats.syntax.flatMap._
  import cats.syntax.functor._

  def sumIntOptionals[Optional[_]: CatsMonad](oa: Optional[Int], ob: Optional[Int]): Optional[Int] = {
    for {
      a <- oa
      b <- ob
    } yield a + b
  }

  // Blows up the stack!
  def sumNaive(list: List[Optional[Int]]): Optional[Int] = {
    list match {
      case Nil => catsMonadInstance.pure(0)
      case head :: tail =>
        head.flatMap { headValue =>
          sumNaive(tail).map { restValue =>
            headValue + restValue
          }
        }
    }
  }

  // Works provided that
  def sumTailRecM(list: List[Optional[Int]]): Optional[Int] = {
    case class Params(elements: List[Optional[Int]], accumulator: Int)
    catsMonadInstance.tailRecM(Params(list, 0)) { case Params(xs, acc) =>
      xs match {
        case Nil => catsMonadInstance.pure(Right(acc))
        case head :: tail =>
          head.map { headValue =>
            Left(Params(tail, headValue + acc))
          }
      }
    }
  }

  def productTailRecM(list: List[Optional[Int]]): Optional[Int] = {
    val m = catsMonadInstance
    case class Params(list: List[Optional[Int]], accumulator: Int)
    m.tailRecM[Params, Int](Params(list, 1)) { params =>
      params.list match {
        case Nil =>
          m.pure(Right(params.accumulator))
        case head :: tail =>
          head.map { headValue =>
            Left(Params(tail, headValue * params.accumulator))
          }
      }
    }
  }

  def concatNaive(list: List[Optional[String]]): Optional[String] = {
    val m = catsMonadInstance
    list match {
      case Nil => m.pure("")
      case maybeHead :: tail =>
        maybeHead match {
          case Presence(_) =>
            maybeHead.flatMap { head =>
              concatNaive(tail).map { rest =>
                head + rest
              }
            }
          case Absence =>
            concatNaive(tail)
        }

    }
  }

  def concatTailRecM(list: List[Optional[String]]): Optional[String] = {
    val m = catsMonadInstance
    case class Params(list: List[Optional[String]], accumulator: StringBuilder)
    m.tailRecM[Params, String](Params(list, new StringBuilder)) { params =>
      params.list match {
        case Nil => m.pure(Right(params.accumulator.toString()))
        case head :: tail =>
          head match {
            case Absence => m.pure(Left(Params(tail, params.accumulator)))
            case Presence(headValue) =>
              m.pure(Left(Params(tail, params.accumulator.append(headValue))))
          }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val xs = 1.to(20000).map(num => if (num % 2 == 0) Presence(num.toString) else Absence).toList
    println(concatTailRecM(xs))
//    println(sumNaive(xs))
  }

}
