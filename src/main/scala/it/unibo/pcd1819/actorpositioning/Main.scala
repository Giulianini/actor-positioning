package it.unibo.pcd1819.actorpositioning

import akka.actor.{Actor, ActorSystem, DeadLetter, Props}
import com.typesafe.config.ConfigFactory
import it.unibo.pcd1819.actorpositioning.actors.{ControllerFSM, ViewActor}
import it.unibo.pcd1819.actorpositioning.model.{Environment, Particle, Vector2D}

class DeadLetterListener extends Actor {
    override def receive: Receive = {
        case d: DeadLetter => println("DeadLetter: " + d.message + " by " + d.sender + " aimed at " + d.recipient)
    }
}

object Main extends App {
    val system = ActorSystem("root", ConfigFactory.parseString("""akka.loglevel = "DEBUG""""))
    system.eventStream.subscribe(system.actorOf(Props(classOf[DeadLetterListener])), classOf[DeadLetter])

    //val controller = system actorOf(ControllerFSM props, "controller")

    val environment = new Environment
    environment.add(Particle(Vector2D(0, 0), 50.0, 34)(1))
    val view = system actorOf(ViewActor props, "view")
    view ! ViewActor.Publish(environment)
}
