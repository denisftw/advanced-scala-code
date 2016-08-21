import shapeless.ops.hlist.Mapper

package base {
  sealed trait Base extends Product with Serializable
  case class User(firstName: String, lastName: String) extends Base
  case class Credentials(username: String, password: String) extends Base
}


/**
  * Created by denis on 8/18/16.
  */
object HListMain {

  def main(args: Array[String]) {

    {
      import shapeless._
      val hlist = 1 :: "string" :: HNil
      println(hlist)

      object upperPoly extends Poly1 {
        implicit def string = at[String](_.toUpperCase)
        implicit def default[A] = at[A](identity)
      }

      val updated = hlist.map(upperPoly)
      println(updated)

      def makeUpperCase[T <: HList](list: T)(implicit mapper:
        Mapper.Aux[upperPoly.type, T, T]): mapper.Out = list.map(upperPoly)

      val list = updated.unify
      println(list)
    }

    {
      import shapeless.syntax.std.tuple._

      val pair = ("str", 3)
      val it = pair.productIterator
      val added = it ++ Iterator(3.14)
      println(added.toList)

      val addedS = pair :+ 3.14
      println(addedS.reverse.head)

      val tupleHList = addedS.productElements
      println(tupleHList)

      println(tupleHList.tupled)
    }

    {
      import shapeless._
      import _root_.base._

      val user = User("Joe", "Black")
      val credentials = Credentials("joe", "password123")
      val userInfo = user :: credentials :: HNil

      val infoList = userInfo.toList
      println(infoList)
    }

    {
      val stringE: Either[Int, String] = Right("Joe")
      val intE: Either[Int, String] = Left(42)
    }

    {
      import shapeless._

      type ESI = Exception :+: String :+: Int :+: CNil

      val excESI = Coproduct[ESI](new Exception)
      val strESI = Coproduct[ESI]("Joe Black")
      val intESI = Coproduct[ESI](42)

      println(strESI)
      // prints Inr(Inl(Joe Black))
      println(strESI.select[String])
      // prints Some(Joe Black)

      object incPoly extends Poly1 {
        implicit def string = at[Int](_ + 1)
        implicit def default[A] = at[A](identity)
      }

      val updIntESI = intESI.map(incPoly)
      println(updIntESI.select[Int])
    }

    {
      import shapeless._

      case class Person(firstName: String, id: Long, age: Int)
      val person = Person("Joe", 1234567L, 42)
      val personGen = Generic[Person]

      val repr = personGen.to(person)
      println(repr)
      // prints Joe :: 1234567 :: 42 :: HNil

      val samePerson = personGen.from(repr)
      println(samePerson)
      // prints Person(Joe,1234567,42)
    }

    {
      import shapeless._

      sealed trait A
      case class B(value: String) extends A
      case class C(value: Int) extends A

      val aGen = Generic[A]

      val b = B("string")
      println(aGen.to(b))
      // Inl(B(string))russian empire

      val c = C(42)
      println(aGen.to(c))
      // Inr(Inl(C(42)))
    }

    {


      import shapeless._
      import shapeless.syntax.std.function._
      import shapeless.ops.function._

      def applyProduct[P <: Product, F, L <: HList, R](p: P)(f: F)
        (implicit gen: Generic.Aux[P, L], fp: FnToProduct.Aux[F, L => R]) =
        f.toProduct(gen.to(p))

      case class UserScore(name: String, score: Int)

      val joeScore = UserScore("joe", 10)
      val lisaScore = UserScore("lisa", 12)
      val total = applyProduct(joeScore, lisaScore)(
        (_: UserScore).score + (_: UserScore).score)
      println(total)
      // prints 22

      val johnScore = UserScore("john", 23)
      val total2 = applyProduct(joeScore, lisaScore, johnScore)(
        (_: UserScore).score + (_: UserScore).score + (_: UserScore).score)
      println(total2)
      // prints 45
    }


  }
}
