object TrampolineDemo {
  object Naive {
    def even[A](lst: List[A]): Boolean = {
      lst match {
        case Nil       => true
        case _ :: tail => odd(tail)
      }
    }

    def odd[A](lst: List[A]): Boolean = {
      lst match {
        case Nil       => false
        case _ :: tail => even(tail)
      }
    }
  }

  sealed trait Trampoline[A]
  case class Done[A](a: A) extends Trampoline[A]
  case class More[A](call: () => Trampoline[A]) extends Trampoline[A]

  def done[A](a: A): Trampoline[A] = Done(a)
  def tailcall[A](call: => Trampoline[A]): Trampoline[A] = More(() => call)

  @scala.annotation.tailrec
  def interpret[A](tr: Trampoline[A]): A = {
    tr match {
      case Done(a) => a
      case More(call) =>
        val next = call()
        interpret(next)
    }
  }

  object Trampolined {
    def evenTC[A](lst: List[A]): Trampoline[Boolean] = {
      lst match {
        case Nil       => done(true)
        case _ :: tail => tailcall(oddTC(tail))
      }
    }

    def oddTC[A](lst: List[A]): Trampoline[Boolean] = {
      lst match {
        case Nil       => done(false)
        case _ :: tail => tailcall(evenTC(tail))
      }
    }

    def even[A](lst: List[A]): Boolean = {
      val program = evenTC(lst)
      interpret(program)
    }
  }

  def main(args: Array[String]): Unit = {
    val list = (1 until 1000000).toList
    println(Trampolined.even(list))
  }
}
