
/**
  * Created by denis on 11/20/16.
  */
sealed abstract class ValidationOp[A]
case class Size(size: Int) extends ValidationOp[Boolean]
case object HasNumber extends ValidationOp[Boolean]

object FreeApplicative {

  def main(args: Array[String]): Unit = {
    import cats.free.FreeApplicative

    type Validation[A] = FreeApplicative[ValidationOp, A]

    import cats.free.FreeApplicative.lift

    def size(size: Int): Validation[Boolean] = lift(Size(size))
    val hasNumber: Validation[Boolean] = lift(HasNumber)


    import cats.implicits._
    val prog: Validation[Boolean] = (size(5) |@| hasNumber).map { case (l, r) => l && r}


    import cats.Id
    import cats.arrow.FunctionK
    import cats.implicits._

    // a function that takes a string as input
    type FromString[A] = String => A


    {
      val compiler = new FunctionK[ValidationOp, FromString] {
        def apply[A](fa: ValidationOp[A]): FromString[A] = str =>
          fa match {
            case Size(size) => str.size >= size
            case HasNumber  => str.exists(c => "0123456789".contains(c))
          }
      }

      val validator = prog.foldMap[FromString](compiler)
      // validator: FromString[Boolean] = scala.Function1$$Lambda$3069/2012160550@3162a52e

      validator("1234")
      // res7: Boolean = false

      validator("12345")
    }

    {
      import cats.data.Kleisli
      import cats.implicits._
      import scala.concurrent.Future
      import scala.concurrent.ExecutionContext.Implicits.global

      // recall Kleisli[Future, String, A] is the same as String => Future[A]
      type ParValidator[A] = Kleisli[Future, String, A]

      val parCompiler = new FunctionK[ValidationOp, ParValidator] {
        def apply[A](fa: ValidationOp[A]): ParValidator[A] = Kleisli { str =>
          fa match {
            case Size(size) => Future { str.size >= size }
            case HasNumber => Future { str.exists(c => "0123456789".contains(c)) }
          }
        }
      }

      val parValidation = prog.foldMap[ParValidator](parCompiler)
    }

    {
      import cats.data.Const
      import cats.implicits._

      type Log[A] = Const[List[String], A]

      val logCompiler = new FunctionK[ValidationOp, Log] {
        def apply[A](fa: ValidationOp[A]): Log[A] = fa match {
          case Size(size) => Const(List(s"size >= $size"))
          case HasNumber => Const(List("has number"))
        }
      }

      def logValidation[A](validation: Validation[A]): List[String] =
        validation.foldMap[Log](logCompiler).getConst

      logValidation(prog)
      logValidation(size(5) *> hasNumber *> size(10))
      logValidation((hasNumber |@| size(3)).map(_ || _))
    }

    {
      import cats.data.Prod
      import cats.data.Kleisli
      import cats.implicits._
      import scala.concurrent.Future
      import cats.data.Const
      import scala.concurrent.ExecutionContext.Implicits.global

      type ParValidator[A] = Kleisli[Future, String, A]
      type Log[A] = Const[List[String], A]
      type ValidateAndLog[A] = Prod[ParValidator, Log, A]

      val prodCompiler = new FunctionK[ValidationOp, ValidateAndLog] {
        def apply[A](fa: ValidationOp[A]): ValidateAndLog[A] = fa match {
          case Size(size) =>
            val f: ParValidator[Boolean] = Kleisli(str =>
              Future { str.size >= size })
            val l: Log[Boolean] = Const(List(s"size > $size"))
            Prod[ParValidator, Log, Boolean](f, l)
          case HasNumber =>
            val f: ParValidator[Boolean] = Kleisli(str =>
              Future(str.exists(c => "0123456789".contains(c))))
            val l: Log[Boolean] = Const(List("has number"))
            Prod[ParValidator, Log, Boolean](f, l)
        }
      }

      val prodValidation = prog.foldMap[ValidateAndLog](prodCompiler)
    }

  }

}
