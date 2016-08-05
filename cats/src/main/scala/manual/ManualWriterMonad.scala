package manual

import cats.Monad

/**
  * Created by denis on 8/4/16.
  */

case class Writer[V](run: (List[String], V)) {
  def bind[B](f: V => Writer[B]): Writer[B] = {
    val (log, value) = run
    val (nLog, nValue) = f(value).run
    Writer((log ++ nLog, nValue))
  }

  def bindNaive[B](f: V => Writer[B]): Writer[B] = {
    val (log, value) = run
    f(value)
  }
}


object ManualWriterMonad {


  def main(args: Array[String]) {
    implicit val writerMonad = new Monad[Writer] {
      override def pure[A](x: A): Writer[A] = Writer((List(), x))
      override def flatMap[A, B](fa: Writer[A])(f: (A) => Writer[B]): Writer[B] = fa.bind(f)
    }

    def greetW(name: String, logged: Boolean) =
      Writer(List("Composing a greeting"), {
        val userName = if (logged) name else "User"
        s"Hello $userName"
      })
    def isLoggedW(name: String) =
      Writer(List("Checking if user is logged in"), name.length == 3)

    val name = "Joe"

    import cats.syntax.flatMap._
    import cats.syntax.functor._

    val resultW = for {
      logged <- isLoggedW(name)
      greeting <- greetW(name, logged)
    } yield greeting

    val (log, result) = resultW.run
    println(log)
    println(result)
  }
}
