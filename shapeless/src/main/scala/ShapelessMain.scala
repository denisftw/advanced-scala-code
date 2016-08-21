/**
  * Created by denis on 8/17/16.
  */
package byo {

  package list {
    // List
    sealed trait MyList[+T] {
      def ::[TT >: T](el: TT): MyList[TT] = MyCons(el, this)
    }

    case object MyNil extends MyList[Nothing]

    case class MyCons[+T](head: T, tail: MyList[T]) extends MyList[T]
  }

  package hlist {
    // HList
    sealed trait HList

    case object HNil extends HList {
      def ::[T](el: T): HCons[T, HNil.type] = HCons(el, this)
    }

    case class HCons[+H, +T <: HList](head: H, tail: T) extends HList {
      def ::[F](el: F): HCons[F, HCons[H, T]] = HCons(el, this)
    }
  }

  package operations {
    trait Operation[T] {
      type Result
      def apply(t: T): Result
    }

    object Operation {
      type Aux[T0, Result0] = Operation[T0] { type Result = Result0 }
    }

    trait Op[T, Result] {
      def apply(t: T): Result
    }
  }
}

object ShapelessMain {

  def main(args: Array[String]) {

    {
      import _root_.byo.list._

      val ints = 3 :: 2 :: MyNil
      println(ints)

      val list = 3 :: "str" :: MyNil
      println(list)
    }

    {
      import _root_.byo.hlist._

      val hList = 3 :: "str" :: HNil
      println(hList)
    }

    {
      import _root_.byo.operations._

      implicit val strLen = new Operation[String] {
        type Result = Int
        def apply(t: String): Result = t.length
      }
      println(strLen("hello"))

      implicit val intInc = new Operation[Int] {
        type Result = Int
        def apply(t: Int): Result = t + 1
      }
      println(intInc(5))

      def applyOps[T, R](t: T)(implicit op: Operation.Aux[T, R], op2: Operation[R]):
      op2.Result = op2.apply(op.apply(t))
      println(applyOps("hello"))
    }

  }
}
