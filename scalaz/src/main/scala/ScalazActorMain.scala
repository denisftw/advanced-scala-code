import scalaz.concurrent.{Strategy, Actor}
import scalaz.concurrent.Actor._


case class Message(text: String)


object ScalazActorMain {

    def main (args: Array[String]): Unit = {
        val myActor = Actor.actor[Message](message => {
            println(s"Received message: ${message.text}")
        }, error => {
            System.err.println(error.getMessage)
        })/*(Strategy.DefaultStrategy)*/

        myActor ! Message("HOHO")

        Thread.sleep(5000)
    }
}
