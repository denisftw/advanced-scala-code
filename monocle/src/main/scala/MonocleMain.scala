import monocle.{Iso, Lens, Prism}

import spire.math.Rational

/**
  * Created by denis on 8/5/16.
  */

object MonocleMain {

  def main(args: Array[String]) {

    case class Person(firstName: String, lastName: String, age: Int, contacts: ContactInfo)
    case class ContactInfo(email: String, phone: String)
    val person = Person("Joe", "Black", 42, ContactInfo("joe@example.com", "5551234"))

    // Getter
    val ageLens = Lens[Person, Int](_.age)(a => person => person.copy(age = a))
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
    def fahr2cel(fahrenheit: Fahrenheit): Celsius = Celsius(fahrenheit.value * r"5/9" - 32)

    println(cel2fahr(Celsius(20)))    // prints Fahrenheit(68)
    println(fahr2cel(Fahrenheit(90))) // prints Celsius(18)

    val cel2FahrIso = Iso[Celsius, Fahrenheit](cel2fahr)(fahr2cel)

    println(cel2FahrIso.get(Celsius(20)))           // prints Fahrenheit(68)
    println(cel2FahrIso.reverseGet(Fahrenheit(90))) // prints Celsius(18)

    case class Kelvin(value: Rational)
    def cel2kel(celsius: Celsius): Kelvin = Kelvin(celsius.value + r"273.15")
    def kel2cel(kelvin: Kelvin): Celsius = Celsius(kelvin.value - r"273.15")
    val cel2KelIso = Iso[Celsius, Kelvin](cel2kel)(kel2cel)

    // Getting Kelvin to Fahrenheit for free
    val kel2celIso = cel2KelIso.reverse
    val kel2FahrIso = kel2celIso.composeIso(cel2FahrIso)
    println(kel2FahrIso.get(Kelvin(r"273.15")))   // prints Fahrenheit(32)

    // Prism
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
}
