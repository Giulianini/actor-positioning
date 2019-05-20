package it.unibo.pcd1819.actorpositioning

import akka.actor.{Actor, ActorSystem, DeadLetter, Props}
import com.typesafe.config.ConfigFactory
import it.unibo.pcd1819.actorpositioning.actors.ControllerActor

object Main extends App {

    val system = ActorSystem("root", ConfigFactory.parseString("""akka.loglevel = "DEBUG""""))
    system.eventStream.subscribe(system.actorOf(Props(classOf[DeadLetterListener])), classOf[DeadLetter])

    val controller = system actorOf(ControllerActor props, "controller")

    controller ! ControllerActor.Init

}

class DeadLetterListener extends Actor {
    override def receive: Receive = {
        case d: DeadLetter => println("DeadLetter: " + d.message + " by " + d.sender + " aimed at " + d.recipient)
    }
}
