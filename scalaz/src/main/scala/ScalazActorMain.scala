import akka.Message

import scalaz.concurrent.{Actor, Strategy}
import scalaz.concurrent.Actor._


class MyActor {
    private var counter = 0
    def handler(message: Message): Unit = {
        counter += 1
        println(s"#$counter: ${message.text}")
    }
}
object MyActor {
    def create: Actor[Message] = Actor.actor(new MyActor().handler)
}


object ScalazActorMain {

    def main (args: Array[String]): Unit = {
/*
        val actors = Array(MyActor.create, MyActor.create)

        1.to(20).foreach { i =>
            val actor = if (i % 2 == 0) actors(0) else actors(1)
            actor ! Message(s"Message $i")
            Thread.sleep(100)
        }*/

        val actor = Actor.actor( (message: Message) => {
            println(s"Received message: ${message.text}")
        }, error => {
            System.err.println(error.getMessage)
        })(Strategy.Sequential)

        actor ! Message("HOHO")

//        Thread.sleep(5000)
    }
}
