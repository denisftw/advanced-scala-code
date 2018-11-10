import scalaz.\/

/**
  * Created by denis on 9/4/16.
  */
class GenerationException(number: Long, message: String)
  extends Exception(message)

object ScalazDisjunction {

  def queryNextNumberE: Exception \/ Long = {
    val source = Math.round(Math.random * 100)
    if (source <= 60) \/.right(source)
    else \/.left(new Exception("The generated number is too big!"))
  }

  def queryNextNumberT: Throwable \/ Long = \/.fromTryCatchNonFatal {
    val source = Math.round(Math.random * 100)
    if (source <= 60) source
    else throw new Exception("The generated number is too big!")
  }

  def queryNextNumberGE: GenerationException \/ Long = \/.fromTryCatchThrowable[Long, GenerationException] {
    val source = Math.round(Math.random * 100)
    if (source <= 90) source
    else throw new GenerationException(source, "The generated number is too big!")
  }

  def main(args: Array[String]): Unit = {

    val lst = List(queryNextNumberGE, queryNextNumberGE, queryNextNumberGE)

    import scalaz._
    import Scalaz._
    val lstD = lst.sequence
    println(lstD)
  }
}
