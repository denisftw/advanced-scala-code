trait Monad[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}
object MonadSyntax {
  implicit class toMonad[F[_], A](fa: F[A])(implicit monadEv: Monad[F]) {
    def map[B](f: A => B): F[B] = monadEv.map(fa)(f)
    def flatMap[B](f: A => F[B]): F[B] = monadEv.flatMap(fa)(f)
  }
}
trait Optional[+A] {
  def extract: A
  def transform[B](f: A => B): Optional[B]
  def bind[B](f: A => Optional[B]): Optional[B]
}
case class Presence[A](a: A) extends Optional[A] {
  override def transform[B](f: A => B): Optional[B] = Presence(f(a))
  override def bind[B](f: A => Optional[B]): Optional[B] = f(a)
  override def extract: A = a
}
case object Absence extends Optional[Nothing] {
  override def transform[B](f: Nothing => B): Optional[B] = Absence
  override def bind[B](f: Nothing => Optional[B]): Optional[B] = Absence
  override def extract: Nothing = throw new NullPointerException
}

object Optional {
  implicit val monadInstance: Monad[Optional] = new Monad[Optional] {
    override def map[A, B](fa: Optional[A])(f: A => B): Optional[B] = {
      fa.transform(f)
    }
    override def flatMap[A, B](fa: Optional[A])(f: A => Optional[B]): Optional[B] = {
      fa.bind(f)
    }
  }
}

object ManualMonadDemo {
//  def sumIntOptionals(oa: Optional[Int], ob: Optional[Int])(implicit monadEv: Monad[Optional]): Optional[Int] = {
  def sumIntOptionals[Optional[_]: Monad](oa: Optional[Int], ob: Optional[Int]): Optional[Int] = {
    // 1. desugared
    val monadEv = implicitly[Monad[Optional]]
    monadEv.flatMap(oa) { a =>
      monadEv.map(ob) { b =>
        a + b
      }
    }
    // 2. for comprehension
    import MonadSyntax._
    for {
      a <- oa
      b <- ob
    } yield a + b
  }

  def sumNaive(list: List[Optional[Int]])(implicit monad: Monad[Optional]): Optional[Int] = {
    list match {
      case head :: tail =>
        sumIntOptionals(head, sumNaive(tail))
      case Nil => Presence(0)
    }
  }

  def sumTailRec(list: List[Optional[Int]])(implicit monad: Monad[Optional]): Optional[Int] = {
    @scala.annotation.tailrec
    def loop(list: List[Optional[Int]], acc: Optional[Int])(implicit monad: Monad[Optional]): Optional[Int] = {
      list match {
        case head :: tail => loop(tail, sumIntOptionals(head, acc))
        case Nil          => acc
      }
    }
    loop(list, Presence(0))
  }

  def sumTrampolined(list: List[Optional[Int]])(implicit monad: Monad[Optional]): Optional[Int] = {
    import scala.util.control.TailCalls._
    def loop(list: List[Optional[Int]]): TailRec[Optional[Int]] = {
      list match {
        case Nil => done(Presence(0))
        case head :: tail =>
          tailcall {
            loop(tail).map(tailSum => sumIntOptionals(head, tailSum))
          }
      }
    }
    loop(list).result
  }

  def main(args: Array[String]): Unit = {
    val xs = 1.to(10000).map(Presence.apply).toList
    sumTailRec(xs)
//    sumNaive(xs) // Will blow up!
    val total = sumTrampolined(xs)
    println(total)
  }
}
