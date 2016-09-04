
import cats.{Foldable, Semigroup}

/**
  * Created by denis on 8/13/16.
  */
object ValidatedMain {

  def main(args: Array[String]) {

    case class Person(name: String, email: String)
    val person = Person("Joe", "joe@example.com")

    import cats.data.Validated
    def checkName(person: Person): Validated[String, String] =
      Validated.invalid("The user with this name doesn't exist")
    def checkEmail(person: Person): Validated[String, String] =
      Validated.invalid("This email looks suspicious")

    import cats.syntax.cartesian._
    import cats.instances.string.catsKernelStdMonoidForString
    val resultVC = checkEmail(person) |@| checkName(person)
    val resultV: Validated[String, String] = resultVC.map(_ + _)
    resultV.fold(
      errors => println(errors),
      str => ()
    )

    import cats.data.ValidatedNel
    type ErrorOr[+A] = ValidatedNel[String, A]
    def checkNameNel(person: Person): ErrorOr[String] =
      Validated.invalidNel("The user with this name doesn't exist")
    def checkEmailNel(person: Person): ErrorOr[String] =
      Validated.invalidNel("This email looks suspicious")

    import cats.instances.list.catsStdInstancesForList
    val resultNelC = checkEmailNel(person) |@| checkNameNel(person)
    val resultNel: ErrorOr[String] = resultNelC.map(_ + _)
    resultNel.fold(
      nel => println(nel.toList),
      str => ()
    )
  }
}
