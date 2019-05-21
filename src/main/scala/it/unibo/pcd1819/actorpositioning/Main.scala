package it.unibo.pcd1819.actorpositioning

import akka.actor.{Actor, ActorSystem, DeadLetter, Props}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import it.unibo.pcd1819.actorpositioning.actors.ControllerActor

class DeadLetterListener extends Actor {
    override def receive: Receive = {
        case d: DeadLetter => println("DeadLetter: " + d.message + " by " + d.sender + " aimed at " + d.recipient)
    }
}

object Main extends App {

    val system = ActorSystem("root", ConfigFactory.parseString("""akka.loglevel = "DEBUG""""))
    system.eventStream.subscribe(system.actorOf(Props(classOf[DeadLetterListener])), classOf[DeadLetter])

    val controller = system actorOf(ControllerActor props, "controller")
    controller ! ControllerActor.Start

    import scala.concurrent.duration._
    import system.dispatcher
    system.scheduler.schedule(0 milliseconds, 1000 milliseconds, controller, ControllerActor.Step)

}
