package manual




trait Monoid[A] {
    def compose(a: A, b: A): A
    def empty: A
}

object DefaultMonoids {
    implicit val stringConcatMonoid = new Monoid[String] {
        override def compose(a: String, b: String): String = s"$a$b"
        override def empty: String = ""
    }
}

object Concatenate {
    def combineAll[A](list: List[A])(implicit monoid: Monoid[A]): A = {
        list.foldRight(monoid.empty)((a,b) => monoid.compose(a,b))
    }
}

object CatsMonoidManual {

    def main(args: Array[String]): Unit = {

        import DefaultMonoids._
        val result = Concatenate.combineAll(List("a", "b", "cc"))

        println(result)
    }
}
