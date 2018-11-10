object PartialFunctions {
  def main(args: Array[String]): Unit = {
    val pf12: PartialFunction[String, Int] = {
      case "one" => 1
      case "two" => 2
    }
    val pf34: PartialFunction[String, Int] = {
      case "three" => 3
      case "four" => 4
    }
    val pf1234 = pf12.orElse(pf34)
    println(pf1234("four"))       // 4
    // println(pf1234("five"))    // scala.MatchError
    println(pf1234.lift("five"))  // None
  }
}
