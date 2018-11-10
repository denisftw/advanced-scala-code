import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object DoobieIOApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    // Resource[IO, HikariTransactor[IO]]
    val transactor: Resource[IO, HikariTransactor[IO]] = for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      te <- ExecutionContexts.cachedThreadPool[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver", "jdbc:postgresql:doobieworld",
        "doobieuser", "doobiepass", ce, te)
    } yield xa

    val districtC = sql"select district from city where name = 'Canberra'".
      query[String].unique

    transactor.use { xa =>
      for {
        district <- districtC.transact(xa)
        _ <- IO(println(district))
      } yield ExitCode.Success
    }
  }
}
