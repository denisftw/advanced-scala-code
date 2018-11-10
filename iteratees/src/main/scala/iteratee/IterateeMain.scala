package iteratee

import scala.util.{Failure, Success}

object IterateeMain {
  def fileExample(): Unit = {
    import io.iteratee.monix.task._
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
    val dataT = wordsE.through(noEmptyLinesEE).
      through(toLowerEE).into(countWordsI).map { dataMap =>
      dataMap.toList.sortWith( _._2 > _._2).take(5).map(_._1)
    }
    import monix.execution.Scheduler.Implicits.global
    dataT.runOnComplete {
      case Success(data) => println(data)
      case Failure(th) => th.printStackTrace()
    }

    /*val lines = linesEn.through(filterEnee).into(takeI[String](100)).unsafePerformSyncAttempt

    lines.map { lns =>
        println(lns)
    }*/
  }


  def main(args: Array[String]) {
    import io.iteratee.modules.id._

    // Just one Int
    val singleNumE = enumOne(42)
    val singleNumI = takeI[Int](1)
    val singleNumResult = singleNumE.into(singleNumI)
    println(singleNumResult)

    // Incrementing one Int
    val incrementNumEE = map[Int, Int](_ + 1)
    val incrementedNumResult = singleNumE.through(incrementNumEE).into(singleNumI)
    println(incrementedNumResult)

    // First 10 even numbers
    val naturalsE = iterate(1)(_ + 1)
    val moreThan100EE = filter[Int](_ >= 100)
    val evenFilterEE = filter[Int](_ % 2 == 0)
    val first10I = takeI[Int](10)
    println(naturalsE.through(moreThan100EE).through(evenFilterEE).into(first10I))

    {
      import io.iteratee.modules.eval._
      // Summing N first numbers
      val naturalsE = iterate(1)(_ + 1)
      val limit1kEE = take[Int](30000)
      val sumI = fold[Int, Int](0) { (acc, next) => acc + next }
      println(naturalsE.through(limit1kEE).into(sumI).value)
    }

    fileExample()

  }
}
