package iteratee


object IterateeMain {


  def fileExample(): Unit = {
    import io.iteratee.scalaz._
    import java.io.File

    val wordsE = readLines(new File("license.txt")).flatMap { line =>
      enumIndexedSeq(line.split("\\W"))
    }
    val noEmptyLinesEE = filter[String](str => str.trim.length > 0)
    val toLowerEE = map[String, String](_.toLowerCase)
    val countWordsI = fold[String, Map[String, Int]](Map.empty) { (acc, next) =>
      acc.get(next) match {
        case None => acc + (next -> 1)
        case Some(num) => acc + (next -> (1 + num))
      }
    }
    val dataT = wordsE.mapE(noEmptyLinesEE).mapE(toLowerEE).run(countWordsI)
    val data = dataT.unsafePerformSync.toList.sortWith( _._2 > _._2).take(5).map(_._1)
    println(data)

    /*val lines = linesEn.mapE(filterEnee).run(takeI[String](100)).unsafePerformSyncAttempt

    lines.map { lns =>
        println(lns)
    }*/
  }


  def main(args: Array[String]) {
    import io.iteratee.pure._

    // Just one Int
    val singleNumE = enumOne(42)
    val singleNumI = takeI[Int](1)
    val singleNumResult = singleNumE.run(singleNumI)
    println(singleNumResult)

    // Incrementing one Int
    val incrementNumEE = map[Int, Int](_ + 1)
    val incrementedNumResult = singleNumE.mapE(incrementNumEE).run(singleNumI)
    println(incrementedNumResult)

    // First 10 even numbers
    val naturalsE = iterate(1)(_ + 1)
    val moreThan100EE = filter[Int](_ >= 100)
    val evenFilterEE = filter[Int](_ % 2 == 0)
    val first10I = takeI[Int](10)
    println(naturalsE.mapE(moreThan100EE).mapE(evenFilterEE).run(first10I))

    {
      import io.iteratee.eval._
      // Summing N first numbers
      val naturalsE = iterate(1)(_ + 1)
      val limit1kEE = take[Int](30000)
      val sumI = fold[Int, Int](0) { (acc, next) => acc + next }
      println(naturalsE.mapE(limit1kEE).run(sumI).value)
    }

    fileExample()

  }
}
