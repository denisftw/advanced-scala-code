import cats.kernel.Monoid


object CatsTypeClasses {
  def main(args: Array[String]): Unit = {
      import cats.implicits.stringMonoid
      val result = Monoid[String].combineAll(List("a", "b", "cc"))
      println(result)
  }
}
