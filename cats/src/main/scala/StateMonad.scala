import cats.data.State

import scala.util.Random

/**
  * Created by denis on 8/4/16.
  */
object StateMonad {


def getIntXorShift(seed: Int): Int = {
  var x = seed
  x ^= (x << 21)
  x ^= (x >>> 35)
  x ^= (x << 4)
  x
}

def genRandomChar: Char = {
  (Random.nextInt(26) + 65).toChar
}

def genChar(seed: Int): (Int, Char) = {
  val newSeed = getIntXorShift(seed)
  val number = Math.abs(newSeed % 25) + 65
  (newSeed, number.toChar)
}


  def main(args: Array[String]) {

    println(List(genRandomChar, genRandomChar, genRandomChar).mkString)


    def nextChar(seed: Int): (Int, Char) = genChar(seed)

    val initialSeed = 42
    val random = {
      val (nextSeed1, first) = nextChar(initialSeed)
      val (nextSeed2, second) = nextChar(nextSeed1)
      val (_, third) = nextChar(nextSeed2)
      List(first, second, third)
    }

    println(random.mkString)


    import cats.data
    val nextCharS = data.State(genChar)

    val randomS = for {
      first <- nextCharS
      second <- nextCharS
      third <- nextCharS
    } yield List(first, second, third)


    val randomSDS = nextCharS.flatMap( first =>
      nextCharS.flatMap( second =>
        nextCharS.map { third =>
          val result = List(first, second, third)
          result
        }
      )
    )


    val result = randomS.runA(initialSeed)
    println(result.value.mkString)



  }
}
