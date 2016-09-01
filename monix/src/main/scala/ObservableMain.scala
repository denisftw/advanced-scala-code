

/**
  * Created by denis on 9/1/16.
  */
object ObservableMain {

  import java.io.Closeable
  /*def using[A](closeable: Closeable)(block: => A): A = {
    try {
      block
    } finally {
      closeable.close()
    }
  }*/
  def using[A, C <: { def close() }](closeable: C)(block: => A): A = {
    try {
      block
    } finally {
      closeable.close()
    }
  }

  def main(args: Array[String]): Unit = {
    import java.io.{BufferedReader, FileReader}
    val br = new BufferedReader(new FileReader("license.txt"))

    using(br) {
      import monix.reactive.Observable
      val linesO = Observable.fromLinesReader(br)
      val wordsO = linesO.flatMap { line =>
        val arr = line.split("\\W").map(_.toLowerCase)
          .map(_.trim).filter(!_.isEmpty)
        Observable.fromIterable(arr.toIterable)
      }

      val rawResultT = wordsO.foldLeftL(Map.empty[String, Int]) { (acc, next) =>
        acc.get(next) match {
          case None => acc + (next -> 1)
          case Some(num) => acc + (next -> (1 + num))
        }
      }

      val finalResultT = rawResultT.map { map =>
        map.toList.sortWith( _._2 > _._2).take(5).map(_._1)
      }

      import monix.execution.Scheduler.Implicits.global
      val resultCF = finalResultT.runAsync

      import scala.concurrent.Await
      import scala.concurrent.duration._
      val result = Await.result(resultCF, 3.seconds)
      println(result)
      // List(the, or, of, and, to)
    }
  }
}
