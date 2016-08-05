import cats.{Functor, Monad}
import cats.kernel.Monoid

import scala.io.StdIn



case class Cell[A](value: A) {
  def bind[B](f: A => Cell[B]): Cell[B] = f(value)
}



trait ConsoleAction[A] {
  def bind[B](f: A => ConsoleAction[B]): ConsoleAction[B]
}

case class ReadFromConsole() extends ConsoleAction[String] {
  override def bind[B](f: (String) => ConsoleAction[B]): ConsoleAction[B] = {
    val input = StdIn.readLine()
    f(input)
  }
}

case class WriteToConsole(output: String) extends ConsoleAction[Unit] {
  override def bind[B](f: (Unit) => ConsoleAction[B]): ConsoleAction[B] = {
    println(output)
    f()
  }
}





object CatsTypeClasses {
  def main(args: Array[String]): Unit = {
//
//
//    println("Write your name: ")
//    val name = StdIn.readLine()
//    println(s"Hello $name")


    import cats.syntax.flatMap._
    import cats.syntax.functor._

    implicit val consoleMonad = new Monad[ConsoleAction] {
      override def flatMap[A, B](fa: ConsoleAction[A])(f: (A) => ConsoleAction[B]):
        ConsoleAction[B] = fa.bind(f)
      override def pure[A](x: A): ConsoleAction[A] =
        ReadFromConsole().asInstanceOf[ConsoleAction[A]]
    }
/*
    consoleMonad.flatMap(WriteToConsole("Write your name: ")) { _ =>
      consoleMonad.flatMap(ReadFromConsole()) { name =>
        consoleMonad.map(WriteToConsole(s"Hello $name")) { _ =>

        }
      }
    }*/

/*

    for {
      _ <- WriteToConsole("Write your name: ")
      name <- ReadFromConsole()
      _ <- WriteToConsole(s"Hello $name")
    } yield ()
*/




    implicit val cellMonad = new Monad[Cell] {
      override def flatMap[A, B](fa: Cell[A])(f: (A) => Cell[B]): Cell[B] = fa.bind(f)
      override def pure[A](x: A): Cell[A] = Cell(x)
    }

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




/*
      import cats.implicits.stringMonoid
      val result = Monoid[String].combineAll(List("a", "b", "cc"))
      println(result)


//    import cats.std._
    import cats.implicits.intGroup
    import cats.implicits.mapMonoid
    val scores = List(Map("Joe" -> 12, "Kate" -> 21), Map("Joe" -> 10))
    val totals = Monoid[Map[String,Int]].combineAll(scores)
    println(totals)


    implicit val cellFunctor: Functor[Cell] = new Functor[Cell] {
      def map[A, B](fa: Cell[A])(f: A => B) = fa map f
    }

     //FUNCTORS
    import cats.implicits.optionInstance
    val maybeName = Option("Joe")
    println(Functor[Option].map(maybeName)(_.length))
    // prints Some(3)

    def greet(name: String): String = s"Hello $name!"
    println(Functor[Option].lift(greet)(maybeName))
    // prints Some(Hello Joe!)

    import cats.implicits.listInstance
    val users = List("Joe", "Kate")
    println(Functor[List].fproduct(users)(greet).toMap)
    // prints Map(Joe -> Hello Joe!, Kate -> Hello Kate!)

    val optUsers = List(Some("Joe"), None, Some("Kate"))
    val listOptionFunctor = Functor[List].compose(Functor[Option])
    println(listOptionFunctor.map(optUsers)(greet))
    // prints  List(Some(Hello Joe!), None, Some(Hello Kate!))

*/







  }
}
