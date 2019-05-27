package it.unibo.pcd1819.actorpositioning

import akka.actor.{Actor, ActorSystem, DeadLetter, Props}
import com.typesafe.config.ConfigFactory
import it.unibo.pcd1819.actorpositioning.actors.ControllerFSM
import it.unibo.pcd1819.actorpositioning.model.{Particle, Vector2D}

import scala.collection.mutable

class DeadLetterListener extends Actor {
  override def receive: Receive = {
    case d: DeadLetter => println("DeadLetter: " + d.message + " by " + d.sender + " aimed at " + d.recipient)
  }
}

object Main extends App {
  val system = ActorSystem("root", ConfigFactory.parseString("""akka.loglevel = "DEBUG""""))
  system.eventStream.subscribe(system.actorOf(Props(classOf[DeadLetterListener])), classOf[DeadLetter])

  val controller = system actorOf(ControllerFSM.props, "controller")

  val result = mutable.MutableList[Particle](Particle(Vector2D(20, 20), 60, 60, 1))
  Thread.sleep(5000)
  controller ! ControllerFSM.Result(result)

}
