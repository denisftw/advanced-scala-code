package macros

/**
  * Created by denis on 7/29/16.
  */

import simulacrum._

@typeclass trait InfoPrinter[T] {
  def toInfo(value: T): String
}

// Custom user defined class
case class User(name: String, age: Int)

// User defined type class instance
object User {
  implicit val userPrinter = new InfoPrinter[User] {
    override def toInfo(value: User): String = s"[User] (${value.name}, ${value.age})"
  }
}

object SimulacrumTypeClasses {

  def main(args: Array[String]) {

    import InfoPrinter.ops._
    val user = User("Joe", 42)
    println(user.toInfo)

  }
}
