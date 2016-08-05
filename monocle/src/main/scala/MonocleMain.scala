import monocle.Lens

/**
  * Created by denis on 8/5/16.
  */

case class Person(firstName: String, lastName: String, age: Int)

object MonocleMain {

  def main(args: Array[String]) {
    val ageLens = Lens[Person, Int](_.age)(a => person => person.copy(age = a))
    val person = Person("Joe", "Black", 42)



    println(ageLens.get(person))
  }

}
