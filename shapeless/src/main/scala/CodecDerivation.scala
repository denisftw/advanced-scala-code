import shapeless.ops.hlist.{Mapper, ToTraversable}
import shapeless._

/**
  * Created by denis on 8/26/16.
  */
object CodecDerivation {

  object JsonEncoder {
    object addQuotes extends Poly1 {
      implicit def str2str = at[String](str => s""""$str"""")
      implicit def default[A] = at[A](identity)
    }

    def doEncode[T <: HList, R <: HList](s: T)(
        implicit mapper: Mapper.Aux[addQuotes.type, T, R],
        toTraversable: ToTraversable.Aux[R, List, Any]): String = {
      val im = s.map(addQuotes)
      im.mkString("{", ",", "}")
    }

    def encode[A, T <: HList](a: A)(implicit gen: Generic.Aux[A, T]): T = {
      val repr = gen.to(a)
      repr
    }
  }

  def main(args: Array[String]) {

    case class Person(firstName: String, lastName: String, age: Int)
    implicit val gen = Generic[Person]

    val person = Person("Joe", "Black", 42)
    val repr = JsonEncoder.encode(person)
    val json = JsonEncoder.doEncode(repr)

    println(json)
  }
}
