
import cats.{Monad, data}

import scalaz.concurrent.Task

/**
  * Created by denis on 8/3/16.
  */




object KleisliMonad {

class AuthService {
  def isLogged(name: String): Task[Boolean] = Task { name.length == 3 }
}
class UserService {
  def greet(name: String, isLogged: Boolean): Task[String] = Task {
    val actualName = if (isLogged) name else "User"
    s"Hello $actualName"
  }
}
  case class Environment(userName: String, userService: UserService, authService: AuthService)

  def main(args: Array[String]) {

    def isLoggedUser = data.ReaderT[Task, Environment, Boolean] { env =>
      env.authService.isLogged(env.userName)
    }
    def greetUser(logged: Boolean) = data.ReaderT[Task, Environment, String] { env =>
      env.userService.greet(env.userName, logged)
    }

    implicit val taskMonad = new Monad[Task] {
      override def tailRecM[A, B](a: A)(f: (A) => Task[Either[A, B]]):
        Task[B] = defaultTailRecM(a)(f)
      override def flatMap[A, B](fa: Task[A])(f: (A) => Task[B]): Task[B] = fa.flatMap(f)
      override def pure[A](x: A): Task[A] = Task.now(x)
    }
    val resultR = for {
      logged <- isLoggedUser
      greeting <- greetUser(logged)
    } yield greeting

    val environment = Environment("Joe", new UserService, new AuthService)
    println(resultR.run(environment).unsafePerformSync)

    // Using Kleisli->local
    case class ExternalContext(env: Environment)
    val externalContext = ExternalContext(environment)
    def context2env(ec: ExternalContext): Environment = ec.env
    val resultContextR = resultR.local(context2env)
    println(resultContextR.run(externalContext).unsafePerformSync)

  }
}
