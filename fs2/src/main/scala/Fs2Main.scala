import fs2._
import java.nio.file._
import java.util.concurrent.Executors

import cats.effect.IO

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

/**
  * Created by denis on 8/7/16.
  */
object Fs2Main {

  implicit val timer = IO.timer(ExecutionContext.global)
  implicit val strategy = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))
  val blockingContext = ExecutionContext.
    fromExecutor(Executors.newCachedThreadPool())

  def log[A](prefix: String): Pipe[IO, A, A] =
    _.evalMap(a => IO.apply { println(s"$prefix> $a"); a })

  def randomDelays[A](max: FiniteDuration): Pipe[IO, A, A] = _.evalMap { a =>
    val delay = IO.apply(Random.nextInt(max.toMillis.toInt))
    delay.flatMap { d =>
      IO.sleep(d.millis).flatMap(_ => IO.pure(a))
    }
  }

  def main(args: Array[String]) {
    fs2FileExample()
  }

  private def chunkExample(): Unit = {
    import fs2.Chunk

    val ch: Chunk[Int] = Chunk.singleton(42)
    val chL = Chunk.longs(Array[Long](1,2,3,4))

    val stream = Stream.chunk(chL)

    def threadName = Thread.currentThread().getName
    val e1 = Stream.eval(IO{ println(s"$threadName"); 1 })
    e1.compile
  }

  private def fs2StreamExamples(): Unit = {
    implicit val contextShift = IO.contextShift(
      ExecutionContext.Implicits.global)
    val pureStream = Stream.apply(1, 2, 3)
    pureStream.intersperse(1)

    pureStream.compile.toVector

    val e1 = Stream.eval(IO {
      println(s"${Thread.currentThread().getName}"); 1
    })
    e1.flatMap(value => Stream.eval[IO, Int](IO(value + 1)))


    val incPipe: Pipe[Pure, Int, Int] = source => source.map(_ + 1)

    val pureStream2 = Stream.emits[IO, Int](Seq(1))

    val simpleStream = Stream(1, 2, 3).covary[IO]
    val pull = simpleStream
    val resultPull = pull.map { value =>
      value + 1
    }.through(log("log")).compile.toVector.unsafeRunSync()
    println(resultPull)

    import scala.concurrent.duration._

    val periodic = Stream.awakeEvery[IO](500.milliseconds).flatMap(dur => Stream.eval {
      IO {
        println(dur.length)
        dur.length
      }
    }).interruptWhen(Stream.sleep[IO](5.seconds).as(true))

    periodic.compile.toVector.unsafeRunAsync { result =>
      println(result)
    }

    //    Thread.sleep(20000)
  }

  private def fs2FileExample(): Unit = {
    implicit val contextShift = IO.contextShift(ExecutionContext.Implicits.global)

    val filePath = Paths.get("license.txt")
    import fs2.io.file._
    val byteStr = readAll[IO](filePath, blockingContext, 1024)
    val lineStr = byteStr.through(text.utf8Decode).through(text.lines)
    val wordStr = lineStr.flatMap { line =>
      Stream.emits(line.split("\\W"))
    }.map(_.toLowerCase).map(_.trim).filter(!_.isEmpty)
    val resultT = wordStr.fold(Map.empty[String, Int]) { (acc, next) =>
        acc.get(next) match {
          case None => acc + (next -> 1)
          case Some(num) => acc + (next -> (1 + num))
        }
      }.map { dataMap =>
          dataMap.toList.sortWith( _._2 > _._2).take(5).map(_._1)
      }.compile.toVector

    val diff = System.currentTimeMillis()
    println(resultT.unsafeRunSync())
    println(s"Elapsed: ${System.currentTimeMillis() - diff}")
  }
}
