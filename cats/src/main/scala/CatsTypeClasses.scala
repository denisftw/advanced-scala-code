import cats.{Functor, Monad}
import cats.kernel.Monoid

import scala.io.StdIn



case class Cell[A](value: A) {
  def bind[B](f: A => Cell[B]): Cell[B] = f(value)
}




object CatsTypeClasses {
  def main(args: Array[String]): Unit = {
//
//
//    println("Write your name: ")
//    val name = StdIn.readLine()
//    println(s"Hello $name")


/*
    consoleMonad.flatMap(WriteToConsole("Write your name: ")) { _ =>
      consoleMonad.flatMap(ReadFromConsole()) { name =>
        consoleMonad.map(WriteToConsole(s"Hello $name")) { _ =>

        }
      }
    }*/

/*


*/


    import cats.syntax.flatMap._
    import cats.syntax.functor._


    implicit val cellMonad = new Monad[Cell] {
      override def tailRecM[A, B](a: A)(f: (A) => Cell[Either[A, B]]):
        Cell[B] = defaultTailRecM(a)(f)
      override def flatMap[A, B](fa: Cell[A])(f: (A) => Cell[B]): Cell[B] = fa.bind(f)
      override def pure[A](x: A): Cell[A] = Cell(x)
    }

    val newCell = Cell(42).flatMap(a => Cell(a + 1))
    println(newCell)

    val c3 = for {
      c1 <- Cell(42)
      c2 <- Cell(23)
    } yield {
      c1 + c2
    }

    println(c3)

/*

    import cats.implicits.monadCombineSyntax

    for {
      _ <- WriteToConsole("Write your name: ")
      name <- ReadFromConsole()
      _ <- WriteToConsole(s"Hello $name")
    } yield {

    }
*/

    {
      import cats.instances.int.catsKernelStdGroupForInt
      import cats.instances.map.catsKernelStdMonoidForMap

      val scores = List(Map("Joe" -> 12, "Kate" -> 21), Map("Joe" -> 10))
      val totals1 = Monoid[Map[String,Int]].combine(Map("Joe" -> 12, "Kate" -> 21), Map("Joe" -> 10))
      println(totals1)

      val totals2 = Monoid[Map[String,Int]].combineAll(scores)
      println(totals2)
    }


      import cats.instances.string.catsKernelStdMonoidForString
      val result = Monoid[String].combineAll(List("a", "b", "cc"))
      println(result)

    {
      import cats.instances.int.catsKernelStdGroupForInt
      import cats.instances.map.catsKernelStdMonoidForMap
      val scores = List(Map("Joe" -> 12, "Kate" -> 21), Map("Joe" -> 10))
      val totals = Monoid[Map[String,Int]].combineAll(scores)
      println(totals)
    }



    implicit val cellFunctor: Functor[Cell] = new Functor[Cell] {
      def map[A, B](fa: Cell[A])(f: A => B) = fa map f
    }

    def greet(name: String): String = s"Hello $name!"

     //FUNCTORS
    {
      import cats.implicits.catsStdInstancesForOption
      val maybeName = Option("Joe")
      println(Functor[Option].map(maybeName)(_.length))
      // prints Some(3)

      println(Functor[Option].lift(greet)(maybeName))
      // prints Some(Hello Joe!)
    }

    import cats.instances.list.catsStdInstancesForList
    val users = List("Joe", "Kate")
    println(Functor[List].fproduct(users)(greet).toMap)
    // prints Map(Joe -> Hello Joe!, Kate -> Hello Kate!)

    import cats.implicits.catsStdInstancesForOption
    val optUsers = List(Some("Joe"), None, Some("Kate"))
    val listOptionFunctor = Functor[List].compose(Functor[Option])
    println(listOptionFunctor.map(optUsers)(greet))
    // prints  List(Some(Hello Joe!), None, Some(Hello Kate!))







  }
}
