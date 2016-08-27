import CodecDerivation.JsonEncoder.addQuotes
import shapeless.ops.hlist.{Mapper, ToTraversable}
import shapeless._
import shapeless.ops.record.{Keys, Values}

/**
  * Created by denis on 8/26/16.
  */
object CodecDerivation {

  object JsonEncoder {
    object addQuotes extends Poly1 {
      implicit def str2str = at[String](str => s""""$str"""")
      implicit def default[A] = at[A](identity)
    }
    def doEncode[T <: HList, R <: HList, K <: HList, V <: HList](s: T)(
      implicit keyExtractor: Keys.Aux[T, K],
      valueExtractor: Values.Aux[T, V],
      valueMapper: Mapper.Aux[addQuotes.type, V, R],
      toTraversableValues: ToTraversable.Aux[R, List, Any],
      toTraversableKeys: ToTraversable.Aux[K, List, Symbol]
        ): String = {
      val keys = keyExtractor.apply
      val values = valueExtractor.apply(s)

      val quoted = values.map(addQuotes)
      val zipped = keys.toList.zip(quoted.toList)

      val elements = zipped.map { case (label, value) =>
        "\"" + label.name + "\"" + ":" + value.toString
      }
      val json = elements.mkString("{", ",", "}")
      json
    }

    def encode[A, T <: HList](a: A)(implicit gen: LabelledGeneric.Aux[A, T], keys: Keys[T]): T = {
      val repr = gen.to(a)
      repr
    }
  }

  def main(args: Array[String]) {

    case class Person(firstName: String, lastName: String, age: Int)

    val person = Person("Joe", "Black", 42)
    val repr = JsonEncoder.encode(person)
    val json = JsonEncoder.doEncode(repr)

    println(json)
  }
}
