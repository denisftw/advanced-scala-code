package akka

import akka.actor.Actor
import akka.actor.Actor.Receive




case class Message(text: String)

class MyActor extends Actor {
  private var counter = 0
  override def receive = {
    case message: Message =>
      counter += 1
      println(s"#$counter: ${message.text}")
  }
}

object AkkaActorMain {

  def main(args: Array[String]) {

  }
}
