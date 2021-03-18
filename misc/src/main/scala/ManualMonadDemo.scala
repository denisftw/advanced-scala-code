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
    import MonadOps._
    for {
      a <- oa
      b <- ob
    } yield a + b
  }

  def main(args: Array[String]): Unit = {
    val oa: Optional[Int] = Presence(23)
    val ob: Optional[Int] = Presence(27)
    println(sumIntOptionals(oa, ob).extract)
  }
}
