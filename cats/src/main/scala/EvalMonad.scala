import cats.{Eval, Functor, Monad}

/**
  * Created by denis on 8/3/16.
  */


object EvalMonad {

  def sum(num: Long): Long = {
    if (num > 0) {
      sum(num - 1) + num
    } else 0
  }

  def sumEval(num: Long): Eval[Long] = {
    if (num > 0) {
      Eval.defer( sumEval(num - 1) ).map(_ + num)
    } else Eval.now(0)
  }

  def main(args: Array[String]) {
    println(sumEval(30000).value)
  }
}
