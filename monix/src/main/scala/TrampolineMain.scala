
/**
  * Created by denis on 8/31/16.
  */
object TrampolineMain {

  sealed trait Op[T] {
    def map[D](f: T => D): Op[D] = flatMap(f.andThen { result => Now(result) })
    def flatMap[D](f: T => Op[D]): Op[D] = FlatMap(this, f)
  }
  case class Now[T](value: T) extends Op[T]
  case class Later[T](run: () => Op[T]) extends Op[T]
  case class FlatMap[A,B](sub: Op[A], k: A => Op[B]) extends Op[B]

  def main(args: Array[String]): Unit = {

    def sumEval(num: Long): Op[Long] = {
      if (num > 0) {
        Later( () => sumEval(num - 1) ).map(_ + num)
      } else Now(0)
    }

    @annotation.tailrec
    def run[T](op: Op[T]): T = {
      op match {
        case Now(t) => t
        case Later(f) => run(f())
        case FlatMap(x,f) => x match {          // HERE
          case Now(a) => run(f(a))
          case Later(r) => run(FlatMap(r(), f))
          case FlatMap(y, g) =>
            run(FlatMap(y, (a: Any) => FlatMap(g(a), f)))
        }
      }
    }

    println(run(sumEval(30000)))
  }
}
