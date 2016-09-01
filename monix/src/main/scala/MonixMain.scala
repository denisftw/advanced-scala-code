import cats.{Applicative, Cartesian}

/**
  * Created by denis on 8/31/16.
  */
object MonixMain {

  def main(args: Array[String]): Unit = {

    {
      import monix.eval.Coeval

      def sumEval(num: Long): Coeval[Long] = {
        if (num > 0) {
          Coeval.defer( sumEval(num - 1) ).map(_ + num)
        } else Coeval.now(0)
      }

      println(sumEval(30000).value)
    }

    {
      // Applicatives
      import monix.cats._
      import monix.eval.Coeval

      val nameC: Coeval[String] = Coeval.eval("Joe")
      val surnameC: Coeval[String] = Coeval.eval("Black")

      def add(i: String, j: String): String = s"$i $j"

      val sum = Applicative[Coeval].map2(nameC, surnameC)(add)
      println(sum.value)

      {
        // Cartesian product
        import cats.syntax.cartesian._
        println((nameC |@| surnameC).map(add).value)
      }
    }
  }
}
