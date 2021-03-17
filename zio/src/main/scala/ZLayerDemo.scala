import zio.{ExitCode, Has, Task, URIO, ZIO, ZLayer}
object ZLayerDemo extends zio.App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    object Email {
      trait Service {
        def doSendEmail(address: String, message: String): Task[Unit]
      }
      val live = ZLayer.succeed {
        new Service {
          def doSendEmail(address: String, message: String): Task[Unit] = Task {
            println(s"Sending an email to '$address'")
          }
        }
      }
    }

    object Repository {
      trait Service {
        def findUserAddress(id: Long): Task[Option[String]]
      }
      val live = ZLayer.succeed {
        new Service {
          def findUserAddress(id: Long): Task[Option[String]] = Task {
            if (id == 1) Some("denis@appliedscala.com") else None
          }
        }
      }
    }

    def findAddressByUserId(id: Long) = {
      ZIO.accessM[Has[Repository.Service]] { service =>
        service.get.findUserAddress(id)
      }
    }

    def notifyUser(address: String, message: String) = {
      ZIO.accessM[Has[Email.Service]] { service =>
        service.get.doSendEmail(address, message)
      }
    }

    // TODO: Add vertical composition as well

    val env = Email.live ++ Repository.live
    val algorithm = for {
      maybeAddress <- findAddressByUserId(1)
      address <- ZIO.getOrFail(maybeAddress)
      _ <- notifyUser(address, "Hello!!")
    } yield ()

    algorithm
      .provideLayer(env)
      .exitCode
  }
}
