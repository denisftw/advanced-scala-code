import shapeless.ops.hlist.{Mapper, ToTraversable}
import shapeless._
import shapeless.ops.record.{Keys, Values}

/**
  * Created by denis on 8/26/16.
  */
object CodecDerivation {

  object JsonEncoder {

    object quoteStrings extends Poly1 {
      implicit def str2str = at[String](str => s""""$str"""")
      implicit def default[A] = at[A](identity)
    }

    def doEncode[A, T <: HList, K <: HList, V <: HList, R <: HList](a: A)(
      implicit gen: LabelledGeneric.Aux[A, T],
      keyExtractor: Keys.Aux[T, K],
      valueExtractor: Values.Aux[T, V],
      valueMapper: Mapper.Aux[quoteStrings.type, V, R],
      toTraversableValues: ToTraversable.Aux[R, List, Any],
      toTraversableKeys: ToTraversable.Aux[K, List, Symbol]
    ): String = {
      val repr = gen.to(a)
      val keys = keyExtractor.apply
      val values = valueExtractor.apply(repr)

      val quoted = values.map(quoteStrings)
      val zipped = keys.toList.zip(quoted.toList)

      val elements = zipped.map { case (label, value) =>
        "\"" + label.name + "\"" + ":" + value.toString
      }
      val json = elements.mkString("{", ",", "}")
      json
    }
  }

  def main(args: Array[String]) {

    case class Person(firstName: String, lastName: String, age: Int)
    val person = Person("Joe", "Black", 42)

    val json = JsonEncoder.doEncode(person)
    println(json)
    // prints {"firstName":"Joe","lastName":"Black","age":42}
  }
}
