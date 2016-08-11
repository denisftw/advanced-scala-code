import fs2._
import java.nio.file._
import scala.concurrent.duration._

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

/**
  * Created by denis on 8/7/16.
  */
object Fs2Main {

  implicit val strategy = Strategy.fromFixedDaemonPool(2)
  implicit val scheduler = Scheduler.fromFixedDaemonPool(2)

  def log[A](prefix: String): Pipe[Task, A, A] = _.evalMap[Task, Task, A](a => Task.delay { println(s"$prefix> $a"); a })

  def randomDelays[A](max: FiniteDuration): Pipe[Task, A, A] = _.evalMap[Task, Task, A] { a =>
    val delay = Task.delay(Random.nextInt(max.toMillis.toInt))
    delay.flatMap { d => Task.now(a).schedule(d.millis) }
  }

  /*{ inStream =>
    inStream.evalMap[Task, A, A] { el =>
      Task.delay {
        println(el)
        el
      }
    }
  }*/

  def main(args: Array[String]) {
    val filePath = Paths.get("license.txt")
    import fs2.io.file._


    val pureStream = Stream.apply(1, 2, 3)
    pureStream.intersperse(1)


    val std = Trace.Stdout
    pureStream.runTrace(std)

    val e1 = Stream.eval(Task.delay{ println(s"${Thread.currentThread().getName}"); 1 })
    e1.evalMap[Task, Task, Int](value => Task.delay(value + 1))


    val incPipe: Pipe[Pure, Int, Int] = source => source.map(_ + 1)

    val pureStream2 = Stream.emits[Task, Int](Seq(1))

    val simpleStream = Stream(1, 2, 3).covary[Task]
    val pull = simpleStream
    val resultPull = pull.map { value =>
      value + 1
    }.through(log("log")).runLog.unsafeRun
    println(resultPull)


    import scala.concurrent.duration._

    val periodic = time.awakeEvery[Task](500.milliseconds).evalMap[Task, Task, Long](dur => Task {
      println(dur.length)
      dur.length
    }).interruptWhen(Stream.eval(Task.schedule(true, 5.seconds)))

    periodic.runLog.unsafeRunAsync { result =>
      println(result)
    }

//    Thread.sleep(20000)



    val byteStr = readAll[Task](filePath, 1024)
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
      }.runLog

    val diff = System.currentTimeMillis()
    println(resultT.unsafeRun)
    println(s"Elapsed: ${System.currentTimeMillis() - diff}")
  }
}
