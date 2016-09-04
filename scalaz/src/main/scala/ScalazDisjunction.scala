

/**
  * Created by denis on 9/4/16.
  */
object ScalazDisjunction {

  def main(args: Array[String]): Unit = {

    {
      import scalaz.\/
      def queryNextNumber: Exception \/ Long = {
        val source = Math.round(Math.random * 100)
        if (source <= 60) \/.right(source)
        else \/.left(new Exception("The generated number is too big!"))
      }
    }

    {
      import scalaz.\/
      def queryNextNumber: Throwable \/ Long = \/.fromTryCatchNonFatal {
        val source = Math.round(Math.random * 100)
        if (source <= 60) source
        else throw new Exception("The generated number is too big!")
      }
    }

    {
      class GenerationException(number: Long, message: String)
        extends Exception(message)

      import scalaz.\/
      def queryNextNumber: GenerationException \/ Long = \/.fromTryCatchThrowable[Long, GenerationException] {
        val source = Math.round(Math.random * 100)
        if (source <= 90) source
        else throw new GenerationException(source, "The generated number is too big!")
      }

      val lst = List(queryNextNumber, queryNextNumber, queryNextNumber)

      {
        import scalaz._, Scalaz._
        val lstD = lst.sequenceU
        println(lstD)
      }

    }
  }
}
