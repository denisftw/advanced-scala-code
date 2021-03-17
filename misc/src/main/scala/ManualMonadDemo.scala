trait Monad[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}
object MonadOps {
  implicit class toMonad[F[_], A](fa: F[A])(implicit monadEv: Monad[F]) {
    def map[B](f: A => B): F[B] = monadEv.map(fa)(f)
    def flatMap[B](f: A => F[B]): F[B] = monadEv.flatMap(fa)(f)
  }
}
trait Optional[+A]
case class Presence[A](a: A) extends Optional[A]
case object Absence extends Optional[Nothing]

object Optional {
  // TODO: Add Monad[Optional]
  implicit val monadInstance: Monad[Optional] = new Monad[Optional] {
    override def map[A, B](fa: Optional[A])(f: A => B): Optional[B] = {}
    override def flatMap[A, B](fa: Optional[A])(f: A => Optional[B]): Optional[B] = {}
  }
}

object ManualMonadDemo {
  def sumIntOptionals(oa: Optional[Int], ob: Optional[Int])(implicit monadEv: Monad[Optional]): Optional[Int] = {
    // 1. desugared
    monadEv.flatMap(oa) { a =>
      monadEv.map(ob) { b =>
        a + b
      }
    }
    // 2. for comprehension
    import MonadOps._
    for {
      a <- oa
      b <- ob
    } yield a + b
  }

  def main(args: Array[String]): Unit = {
    val oa: Optional[Int] = Presence(23)
    val ob: Optional[Int] = Presence(27)
    sumIntOptionals(oa, ob)
  }
}
