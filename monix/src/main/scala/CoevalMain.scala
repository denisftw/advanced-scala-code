import cats.{Applicative, Cartesian}

/**
  * Created by denis on 8/31/16.
  */
object CoevalMain {

  def main(args: Array[String]): Unit = {
    {
      import monix.eval.Coeval
      val lazyNum = Coeval.evalOnce { println(42); 42 }
      println(lazyNum.value)
      val exc = Coeval.eval( throw new Exception )
    }


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
      import monix.eval.Coeval
      val nameC: Coeval[String] = Coeval.eval("Joe")
      val surnameC: Coeval[String] = Coeval.eval("Black")
      def add(i: String, j: String): String = s"$i $j"
      val sum = Applicative[Coeval].map2(nameC, surnameC)(add)
      println(sum.value)

      // Cartesian product
      println(Coeval.map2(nameC, surnameC)(add).value())
      import cats.syntax.apply._
      println((nameC, surnameC).mapN(add).value())
    }
  }
}
