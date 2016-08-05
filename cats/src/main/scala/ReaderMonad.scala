import cats.Eval
import cats.data.Reader

/**
  * Created by denis on 8/2/16.
  */





object ReaderMonad {

  class AuthService {
    def isLogged(name: String): Boolean = name.length == 3
  }
  class UserService {
    def greet(name: String, isLogged: Boolean): String = {
      val actualName = if (isLogged) name else "User"
      s"Hello $actualName"
    }
  }
  case class Environment(userName: String, userService: UserService, authService: AuthService)


  def main(args: Array[String]) {
    val toUpper = Reader((str: String) => str.toUpperCase)
    val greet = Reader((name: String) => s"Hello $name")

    val combined1 = toUpper.compose(greet)
    val combined2 = toUpper.andThen(greet)

    val result = combined1.run("Joe")

    def isLoggedUser = Reader[Environment, Boolean](env => env.authService.isLogged(env.userName))
    def greetUser(logged: Boolean) = Reader[Environment, String](env => env.userService.greet(env.userName, logged))

    val resultR = for {
      logged <- isLoggedUser
      greeting <- greetUser(logged)
    } yield greeting

    val environment = Environment("Joe", new UserService, new AuthService)
    println(resultR.run(environment))

    println(result)
  }
}
