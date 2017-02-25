import java.util.UUID

import monocle.{Iso, Lens, Optional, Prism}
import spire.math.Rational

/**
  * Created by denis on 8/5/16.
  */

object MonocleMain {

  def main(args: Array[String]) {

    // Lens
    {
      case class Person(firstName: String, lastName: String, age: Int, contacts: ContactInfo)
      case class ContactInfo(email: String, phone: String)

      {
        case class LensM[S, A](get: S => A)(set: A => S => S)
        val ageLens = LensM[Person, Int](_.age)(a => person => person.copy(age = a))
      }

      // Getter
      val ageLens = Lens[Person, Int](_.age)(a => person => person.copy(age = a))

      val person = Person("Joe", "Black", 42, ContactInfo("joe@example.com", "5551234"))
      println(s"Age: ${ageLens.get(person)}")

      val updatedAge = person.copy(age = 43)
      println(s"Age: ${updatedAge.age}")

      // Setter
      val emailLens = Lens[Person, String](_.contacts.email)(a => person => person.copy(contacts = person.contacts.copy(email = a)))
      val updated = emailLens.set("joe.black@example.com")(person)
      println(s"Email: ${emailLens.get(updated)}")

      // Macros
      import monocle.macros.GenLens
      val phoneLens = GenLens[Person](_.contacts.phone)
      val updatedPerson2 = phoneLens.set("5554321")(updated)
      println(s"Phone: ${updatedPerson2.contacts.phone}")

      // Lens composition
      val contactLens = GenLens[Person](_.contacts)
      val phoneContactLens = GenLens[ContactInfo](_.phone)
      val composedLens = contactLens.composeLens(phoneContactLens)
      println(s"Phone: ${composedLens.get(updatedPerson2)}")

      // Simple Iso demonstration
      val caseIso = Iso[String, String](_.toLowerCase)(_.toUpperCase)
      println(caseIso.get("MoNoClE"))
      println(caseIso.reverseGet("MoNoClE"))
    }

    // Iso
    {
      // Celsius to Fahrenheit
      import spire.syntax.literals._
      val temp1 = r"232.112"
      val temp2 = r"222.874"
      val result = temp1 + temp2

      case class CelsiusD(value: Double)
      case class FahrenheitD(value: Double)

      case class Celsius(value: Rational)
      case class Fahrenheit(value: Rational)

      def cel2fahr(celsius: Celsius): Fahrenheit = Fahrenheit(celsius.value * r"9/5" + 32)
      def fahr2cel(fahrenheit: Fahrenheit): Celsius = Celsius((fahrenheit.value - 32) * r"5/9")

      println(cel2fahr(Celsius(20)))    // prints Fahrenheit(68)
      println(fahr2cel(Fahrenheit(68))) // prints Celsius(20)

      val cel2FahrIso = Iso[Celsius, Fahrenheit](cel2fahr)(fahr2cel)

      println(cel2FahrIso.get(Celsius(20)))           // prints Fahrenheit(68)
      println(cel2FahrIso.reverseGet(Fahrenheit(68))) // prints Celsius(20)

      val fahBoilingPoint=cel2FahrIso.get(Celsius(100))
      println (cel2FahrIso.get(cel2FahrIso.reverseGet(fahBoilingPoint)) == fahBoilingPoint)

      case class Kelvin(value: Rational)
      def cel2kel(celsius: Celsius): Kelvin = Kelvin(celsius.value + r"273.15")
      def kel2cel(kelvin: Kelvin): Celsius = Celsius(kelvin.value - r"273.15")
      val cel2KelIso = Iso[Celsius, Kelvin](cel2kel)(kel2cel)

      // Getting Kelvin to Fahrenheit for free
      val kel2celIso = cel2KelIso.reverse
      val kel2FahrIso = kel2celIso.composeIso(cel2FahrIso)
      println(kel2FahrIso.get(Kelvin(r"273.15")))   // prints Fahrenheit(32)
    }


    // Prism
    {
      sealed trait ConfigValue
      case class IntValue(value: Int) extends ConfigValue
      case class StringValue(value: String) extends ConfigValue

      val portNumber: ConfigValue = IntValue(80)
      def offsetPort(port: Int): Int = port + 8000

      {
        val intConfP = Prism[ConfigValue, Int] {
          case IntValue(int) => Some(int)
          case _ => None
        }(IntValue.apply)
        val updatedPort = intConfP.modify(offsetPort)(portNumber)
        println(updatedPort)
      }
    }

    // Optional
    {
      case class User(name: String, age: Int, id: Option[UUID])

      val idO = Optional[User, UUID](_.id)(id => user => user.copy(id = Some(id)))
      // idO: Optional[User, UUID]
      val register = idO.set(UUID.randomUUID())
      // register: (User) => User

      val unregistered = User("Joe", 42, None)
      val registeredUser = register(unregistered)
      println(registeredUser)
      // prints User(Joe,42,Some(24c56ff4-4f03-4b0d-b6b6-33e38460d884))

      val scores = Map("Joe" -> 10, "Alice" -> 12)
      def scoreO(name: String) = Optional[Map[String, Int], Int](
        _.get(name))( score => map => map.updated(name, score) )

      val joeScoreO = scoreO("Joe")
      // joeScoreO: Optional[Map[String, Int], Int]
      val joeScore = joeScoreO.getOption(scores)
      println(joeScore)
      // prints Some(10)

      val johnScoreO = scoreO("John")
      val johnScore = johnScoreO.getOption(scores)
      println(johnScore)
      // prints None

      val updatedScores = johnScoreO.set(15)(scores)
      println(updatedScores)
      // prints Map(Joe -> 10, Alice -> 12, John -> 15)
    }
  }
}
