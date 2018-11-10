/**
  * Created by denis on 8/28/16.
  */
object DoobieMain {
  import scala.concurrent.ExecutionContext
  import doobie._
  import doobie.implicits._
  import cats.implicits._
  import cats.effect.IO

  def main(args: Array[String]) {
    // type ConnectionIO[A] = cats.free.Free[connection.ConnectionOp, A]
    val helloWorld = "Hello World".pure[ConnectionIO]

    implicit val cs = IO.contextShift(ExecutionContext.global)
    val xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver", "jdbc:postgresql:doobieworld",
      "doobieuser", "doobiepass"
    )

    {
      val task = helloWorld.transact(xa).map { str =>
        println("Thread: " + Thread.currentThread().getName)
        str
      }
      println(task.unsafeRunSync)
    }

    {
      val yearC = sql"select extract(year from current_date)".
        query[Int].unique
      val task = yearC.transact(xa)
      println(task.unsafeRunSync)
    }

    multipleQueriesExample(xa)
    shapelessResultExample(xa)
    caseClassResultExample(xa)
    fs2IntegrationExample(xa)
  }

  def multipleQueriesExample(xa: Transactor[IO]): Unit = {
    val districtC = sql"select district from city where name = 'Canberra'".
      query[String].unique
    val populationC = sql"select population from country where name = 'Australia'".
      query[Int].unique

    {
      val dataC = (districtC, populationC).tupled
      val task = dataC.transact(xa)
      println(task.unsafeRunSync)
      // prints (Capital Region,18886000)
    }

    {
      val dataC = for {
        district <- districtC
        population <- populationC
      } yield (district, population)

      val task = dataC.transact(xa)
      println(task.unsafeRunSync)
    }
  }

  def caseClassResultExample(xa: Transactor[IO]): Unit = {
    def findByCode(code: String) =
      sql"""select name, population, headofstate from country where code = $code""".
        query[CountryInfo]

    println(findByCode("SMR").sql)
    // select name, population, headofstate from country where code = ?

    val task = findByCode("SMR").option.transact(xa)
    println(task.unsafeRunSync)
    // prints Some(CountryInfo(San Marino,27000,None))
  }

  def shapelessResultExample(xa: Transactor[IO]): Unit = {
    import shapeless._

    def findByCode(code: String) =
      sql"""select name, population, headofstate from country where code = $code""".
        query[String :: Int :: Option[String] :: HNil]

    println(findByCode("SMR").sql)
    // select name, population, headofstate from country where code = ?

    val task = findByCode("SMR").option.transact(xa)
    println(task.unsafeRunSync)
    // prints Some(San Marino :: 27000 :: None :: HNil)
  }

  def fs2IntegrationExample(xa: Transactor[IO]): Unit = {
    import fs2._

    val countryStream: Stream[ConnectionIO, CountryInfo] =
      sql"""select name, population, headofstate from country""".
        query[CountryInfo].stream
    val streamTask = countryStream.transact(xa)
    // streamTask: Stream[IO, CountryInfo]

    val filtered = streamTask.filter(_.population < 30000).
      map(_.population).fold(0)( (a,b) => a + b )
    val countries = filtered.compile.toVector.unsafeRunSync
    println(countries)
    // prints Vector(215350)
  }
}

case class CountryInfo(name: String, population: Int, headOfState: Option[String])