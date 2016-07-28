package iteratee

import cats.kernel.Monoid


object IterateeMain {


    def fileExample(): Unit = {
        import io.iteratee._, io.iteratee.scalaz._, java.io.File, io.iteratee.files._

        val linesEn = readLines(new File("build.sbt")).flatMap { line =>
            enumVector(line.split("\\W").toVector)
        }
        val noEmptyLinesEnE = filter[String]( str => str.trim.length > 0 )

        val countWordsItE = fold[String, Map[String, Int]](Map.empty) { (acc, next) =>
            acc.get(next) match {
                case None => acc + (next -> 1)
                case Some(num) => acc + (next -> (1+num))
            }
        }

        val dataT = linesEn.mapE(noEmptyLinesEnE).run(countWordsItE).unsafePerformSync

        dataT.map { data =>
            println(data)
        }

        /*val lines = linesEn.mapE(filterEnee).run(takeI[String](100)).unsafePerformSyncAttempt

        lines.map { lns =>
            println(lns)
        }*/
    }


    def main (args: Array[String]) {
//        import io.iteratee.pure._
        import io.iteratee.eval._

        import cats.implicits._


        fileExample()

        implicit val intMonoid = Monoid[Int]

        val naturals = iterate(1)(_ + 1)
        val mult3or5 = filter[Int](i => i % 3 == 0 || i % 5 == 0)
        val finalLimit = takeI[Int](999)

        val foldI = fold[Int, Int](0)( (a,b) => a + b )

        val limit1000 = take[Int](999)

        val lolResult = naturals.mapE(limit1000).mapE(mult3or5).toVector.map(_.sum)

        val lolResult3 = naturals.mapE(limit1000).mapE(mult3or5).run(length)



        val lolResult2 = naturals.mapE(limit1000).mapE(mult3or5).run(foldI).value

        println(s"Alt result: $lolResult2")

        val multsUnder1000 = naturals.mapE(mult3or5).run(finalLimit)


        println(s"Result: ${lolResult.value}")

//        val result = multsUnder1000(naturals)

        val enumeratorStrDuplicate = iterate("a") { value =>
           value + "a"
        }
        val prodInts = iterate(0) { _ + 1 }


        val sameInt = repeat[Int](42)

        val iteratee = takeI[String](5)


        val conIFirst10 = takeI[Int](10)
        val conEFirst10 = take[Int](10)
        val conEMod2 = filter[Int](_ % 2 == 0)


        val idIteratee = identity[String]



        val res = prodInts.mapE(conEMod2).run(conIFirst10)

        val vect = res.value
        println(s"Result: $vect")



    }
}
