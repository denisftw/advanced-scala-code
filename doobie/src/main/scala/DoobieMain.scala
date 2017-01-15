
/**
  * Created by denis on 8/28/16.
  */
object DoobieMain {

  def main(args: Array[String]) {

    import doobie.imports._

    import cats.implicits._
    // type ConnectionIO[A] = cats.free.Free[connection.ConnectionOp, A]
    val helloWorld = "Hello World".pure[ConnectionIO]

    val xa = DriverManagerTransactor[IOLite](
      "org.postgresql.Driver", "jdbc:postgresql:doobieworld",
      "doobieuser", "doobiepass"
    )

    {
      val task = helloWorld.transact(xa)
      println(task.unsafePerformIO)
    }

    {
      val yearC = sql"select extract(year from current_date)".
        query[Int].unique
      val task = yearC.transact(xa)
      println(task.unsafePerformIO)
    }

    {
      val districtC = sql"select district from city where name = 'Canberra'".
        query[String].unique
      val populationC = sql"select population from country where name = 'Australia'".
        query[Int].unique

      {
        val dataC = (districtC |@| populationC).tupled
        val task = dataC.transact(xa)
        println(task.unsafePerformIO)
        // prints (Capital Region,18886000)
      }

      {
        val dataC = for {
          district <- districtC
          population <- populationC
        } yield (district, population)

        val task = dataC.transact(xa)
        println(task.unsafePerformIO)
      }
    }

    {
      import cats.Monad
      import fs2.Task
      implicit val taskMonad = new Monad[Task] {
        override def pure[A](x: A): Task[A] = Task.delay(x)
        override def flatMap[A, B](fa: Task[A])(f: (A) => Task[B]): Task[B] =
          fa.flatMap(f)
        override def tailRecM[A, B](a: A)(f: (A) => Task[Either[A, B]]):
        Task[B] = Task.suspend(f(a)).flatMap {
          case Left(continueA) => tailRecM(continueA)(f)
          case Right(b) => Task.now(b)
        }
      }

      val xa = DriverManagerTransactor[Task](
        "org.postgresql.Driver", "jdbc:postgresql:doobieworld",
        "doobieuser", "doobiepass"
      )

      val task = helloWorld.transact(xa)
      println(task.unsafeRun())
      // prints Hello World
    }

    {
      import shapeless._

      case class CountryInfo(name: String, population: Int, headOfState: Option[String])

      def findByCode(code: String) =
        sql"""select name, population, headofstate from country where code = $code""".
        query[CountryInfo]

      println(findByCode("SMR").sql)
      // select name, population, headofstate from country where code = ?

      val task = findByCode("SMR").option.transact(xa)
      println(task.unsafePerformIO)
      // prints Some(San Marino :: 27000 :: None :: HNil)
    }

    {
      case class CountryInfo(name: String, population: Int, headOfState: Option[String])

      import fs2._
      val countryStream: Stream[ConnectionIO, CountryInfo] =
        sql"""select name, population, headofstate from country""".
          query[CountryInfo].process
      val streamTask = countryStream.transact(xa)
      // streamTask: Stream[IOLite, CountryInfo]


      val filtered = streamTask.filter(_.population < 30000).
        map(_.population).fold(0)( (a,b) => a + b )
      val countries = filtered.runLog.unsafePerformIO
      println(countries)
      // prints Vector(215350)
    }
  }
}
