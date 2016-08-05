import cats.data.{Writer, WriterT}

/**
  * Created by denis on 8/3/16.
  */
object WriterMonad {



  def main(args: Array[String]) {

    def greetW(name: String, logged: Boolean) =
      Writer(List("Composing a greeting"), {
        val userName = if (logged) name else "User"
        s"Hello $userName"
      })
    def isLoggedW(name: String) =
      Writer(List("Checking if user is logged in"), name.length == 3)

    val name = "Joe"

    import cats.std.list._

    val resultW = for {
      logged <- isLoggedW(name)
      greeting <- greetW(name, logged)
    } yield greeting

    val (log, result) = resultW.run
    println(log)
    println(result)

  }
}
