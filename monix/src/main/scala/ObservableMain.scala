import monix.reactive.Consumer

/**
  * Created by denis on 9/1/16.
  */
object ObservableMain {

  def main(args: Array[String]): Unit = {
    import java.io.{BufferedReader, FileReader}
    val br = new BufferedReader(new FileReader("license.txt"))

    arm.ArmUtils.using(br) {
      import monix.reactive.Observable
      val linesO = Observable.fromLinesReader(br)
      val wordsO = linesO.flatMap { line =>
        val arr = line.split("\\W").map(_.toLowerCase)
          .map(_.trim).filter(!_.isEmpty)
        Observable.fromIterable(arr.toIterable)
      }

      val rawResultO = wordsO.foldLeftF(Map.empty[String, Int]) { (acc, next) =>
        acc.get(next) match {
          case None => acc + (next -> 1)
          case Some(num) => acc + (next -> (1 + num))
        }
      }

      val finalResultT = rawResultO.map { map =>
        map.toList.sortWith( _._2 > _._2).take(5).map(_._1)
      }.runWith(Consumer.head)

      import monix.execution.Scheduler.Implicits.global
      val resultCF = finalResultT.runAsync

      import scala.concurrent.Await
      import scala.concurrent.duration._
      val result = Await.result(resultCF, 30.seconds)
      println(result)
      // List(the, or, of, and, to)
    }
  }
}
