import scala.annotation.tailrec


/**
  * Created by denis on 8/12/16.
  */
object ApplicativeMain {

  case class Cell[A](value: A) {
    def map[B](f: A => B): Cell[B] = Cell(f(value))
  }

  def main(args: Array[String]) {

    {
      import cats.Functor
      import cats.instances.option._
      def increment(i: Int): Int = i + 1
      val maybeNum: Option[Int] = Some(42)
      val maybeRes = Functor[Option].map(maybeNum)(increment)
      println(maybeRes)
      // prints Some(43)
    }



    def add(i: Int, j: Int): Int = i + j
    val a: Option[Int] = Some(42)
    val b: Option[Int] = Some(33)

    {
    import cats.Monad
    import cats.instances.option._

    val maybeRes = Monad[Option].flatMap(a) { aa =>
      Monad[Option].map(b) { bb =>
        add(aa, bb)
      }
    }
    println(maybeRes)
    // prints Some(75)
    }

    // lifting 2-argument function for Option
    import cats.Applicative
    import cats.instances.option._
    val result = Applicative[Option].map2(a, b)(add)
    println(result)
    // prints Some(75)

    val c1 = Cell(42)
    val c2 = Cell(33)

    {
      import cats.Monad
      implicit val cellMonad = new Monad[Cell] {
        override def flatMap[A, B](fa: Cell[A])(f: (A) => Cell[B]): Cell[B] = fa.map(f).value
        override def pure[A](x: A): Cell[A] = Cell(x)
        @tailrec override def tailRecM[A, B](a: A)(f: (A) => Cell[Either[A, B]]):
          Cell[B] = f(a) match {
          case Cell(Left(a1)) => tailRecM(a1)(f)
          case Cell(Right(next)) => pure(next)
        }
      }

      import cats.syntax.flatMap._
      import cats.syntax.functor._
      val result = c1.flatMap(cc1 => c2.map(cc2 => add(cc1, cc2)))
      println(result)
      // prints Some(75)
    }



    implicit val cellApplicative = new Applicative[Cell] {
      override def pure[A](x: A): Cell[A] = Cell(x)
      override def ap[A, B](ff: Cell[(A) => B])(fa: Cell[A]): Cell[B] = {
        fa.map(ff.value)
      }
    }
    val resultC = Applicative[Cell].map2(c1, c2)(add)
    println(resultC)
    // prints Cell(75)

    // alternative syntax
    import cats.syntax.cartesian._
    val c3 = (c1 |@| c2).map(add)
    println(c3)
    // prints Cell(75)

    val tuple = (c1 |@| c2).tupled
    println(tuple)
    // prints Cell((42,33))

    traverseTest()

    disjunctions()
  }

  def disjunctions(): Unit = {
    import scalaz._
    import scalaz.std.list._
    import scalaz.syntax.traverse._

    val list: List[\/[Exception, String]] = List(\/-("joe"), \/-("lisa"))
    val dis = list.sequenceU
    println(dis)
  }

  def traverseTest(): Unit = {
    case class Person(username: String, firstName: String)
    def findByName(username: String): Option[Person] = {
      Some(Person(username, username.capitalize))
    }
    val usernames = List("joe", "lisa", "ann", "kate")

    import cats.Traverse
    import cats.instances.list._
    import cats.instances.option._
    val maybeList = Traverse[List].traverse(usernames)(findByName)
    println(maybeList)

    // sequence = traverse(identity)
    import cats.syntax.traverse._
    val maybeListAlt = usernames.map(findByName).sequence

    val maybeListAlt2 = Traverse[List].traverse(usernames.map(findByName))(identity)

    val res = usernames.map(findByName).sequenceU
    println(maybeList)
  }
}
