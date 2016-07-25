

// Type class itself
trait InfoPrinter[T] {
  def toInfo(value: T): String
}

// Default instances
object DefaultInfoPrinters {
  implicit val stringPrinter = new InfoPrinter[String] {
    override def toInfo(value: String): String = s"[String] $value"
  }
  implicit val intPrinter = new InfoPrinter[Int] {
    override def toInfo(value: Int): String = s"[Int] $value"
  }
}

// Singleton approach
object PrintInfo {
  def printInfo[A](value: A)(implicit printer: InfoPrinter[A]): Unit = {
    println(printer.toInfo(value))
  }
}

// Syntax approach (implicit conversions)
object PrintInfoSyntax {
  implicit class PrintInfoOps[T](value: T) {
    def printInfo()(implicit printer: InfoPrinter[T]): Unit = {
      println(printer.toInfo(value))
    }
  }
}

// Custom user defined class
case class User(name: String, age: Int)

// User defined type class instance
object User {
  implicit val userPrinter = new InfoPrinter[User] {
    override def toInfo(value: User): String = s"[User] (${value.name}, ${value.age})"
  }
}


object Main {
  def main(args: Array[String]) = {

    val number = 42

//
    import DefaultInfoPrinters._
//    PrintInfo.printInfo(number)

    import PrintInfoSyntax._
    number.printInfo()


    User("Joe", 42).printInfo()
  }
}