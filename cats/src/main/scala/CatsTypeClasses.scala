import cats.Functor
import cats.data._
import cats.kernel.Monoid
import cats.std.int._
import cats.syntax.semigroup._
import com.sun.xml.internal.bind.v2.model.core.TypeRef

import scala.reflect.ClassTag
/**
  * Created by denis on 7/21/16.
  */

import scala.reflect.runtime.universe._

object CatsTypeClasses {

  case class Person(name: String) {
    def greet: String = s"Hello! I'm $name"
  }
  /*implicit class StringToPerson(str: String) {
    def greet: String = s"Hello! I'm $str"
  }*/

  /*def printContents(list: List): Unit = list.foreach(println(_)) */

  object Person {
    implicit def stringToPerson(str: String): Person = Person(str)
    implicit val person: Person = Person("User")
    implicit val maybePerson: Option[Person] = Some(Person("User"))
  }
  def sayHello(implicit person: Option[Person]): String = s"Hello ${person.map(_.name).getOrElse("Anonymous")}"



//  def paramInfo[T](length: Int)(implicit tag: ClassTag[T]) = new Array[T](length)
  def paramInfo[T: ClassTag](length: Int, element: T) = new Array[T](length)

  def createArray[T: ClassTag](length: Int, element: T) = new Array[T](length)

  def tabulate[T](len: Int, element: T)(implicit m: ClassManifest[T]) = {
    val xs = new Array[T](len)
    xs
  }

  def doStuff(num: Int): Xor[RuntimeException, Int] = Xor.catchOnly[RuntimeException] {
    if (num % 2 == 0) {
      num * 2
    } else throw new Exception("Exception occurred")
  }

  def main(args: Array[String]): Unit = {


//    printContents(List(1,2,3))

    /*val resultX = doStuff(33)
    resultX.fold(
      th => System.err.println(th.getMessage),
      value => println(s"Value: $value")
    )*/

//    implicit val name = "Joe"

/*

    import Person.stringToPerson
    println("Joe".greet)
*/


//    val result = paramInfo(4, 4)
//    println(createArray(5, 'c'))


/*
    val int1M = Monoid[Int].combine(23, 32)
    val int2M = Monoid[Int].combine(0, 11)
    val int3M = int1M |+| int2M
    //println(int3M)

    import cats.std.function._
    // import cats.std.function._
    import cats.syntax.functor._
    // import cats.syntax.functor._
    val func1 = (a: Int) => a + 1
    // func1: Int => Int = <function1>
    val func2 = (a: Int) => a * 2
    // func2: Int => Int = <function1>
    val func3 = func1 map func2
    // func3: Int => Int = <function1>
    println(func3(123))*/
  }
}
