package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}
import it.unibo.pcd1819.actorpositioning.actors.ControllerActor.{Add, Init, Remove, Start}

class ControllerActor extends Actor with ActorLogging{
    override def receive: Receive = {
        case Init =>
            context actorOf(ViewActor props, "view")
            context actorOf(EnvironmentActor props, "environment")
            context become idle
        case _ => ???

    }

    private def idle: Receive = {
        case Start(n) =>
            context actorOf(EnvironmentActor props(n/*, Constants.radius*/), "environment")
            context become running
        case Add(x, y) => ???
        case Remove(x, y) => ???


        case _ => ???

    }

    private def running: Receive = {

        case _ => ???
    }
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
    case object Init
}

object Constants {
    val radius : Double = 100.0
}
