/**
  * Created by denis on 9/1/16.
  */
object ObservableMain {

  def main(args: Array[String]): Unit = {
    import java.io.{BufferedReader, FileReader}
    val br = new BufferedReader(new FileReader("license.txt"))

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

    import monix.reactive.Consumer
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

  import cats.kernel.Monoid
  import monix.reactive.Observable
  def alternativeMonoid(wordsO: Observable[String]): Unit = {
    import cats.instances.int.catsKernelStdGroupForInt
    import cats.instances.map.catsKernelStdMonoidForMap

    val listT = wordsO.map(word => Map(word -> 1)).toListL
    val totals = listT.map { data =>
      Monoid[Map[String, Int]].combineAll(data)
    }
    // totalsT: Task[Map[String, Int]]

    val finalResultT = totals.map { data =>
      data.toList.sortWith( _._2 > _._2).take(5).map(_._1)
    }

    import monix.execution.Scheduler.Implicits.global
    import scala.concurrent.Await
    import scala.concurrent.duration._
    val result = Await.result(finalResultT.runAsync, 30.seconds)
    println(result)
  }
}
