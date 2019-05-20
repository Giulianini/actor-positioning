package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}

class ControllerActor extends Actor with ActorLogging{
    override def receive: Receive = ???
}

object ControllerActor {
    def props = Props(new ControllerActor())

    case class Start(numberOfParticles: Int)
    case class Add(x: Int, y: Int)
    case class Remove(x: Int, y:Int)

    case object Pause
    case object Resume
    case object Stop
    case object Step

}
