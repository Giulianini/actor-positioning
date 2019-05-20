package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import it.unibo.pcd1819.actorpositioning.actors.ControllerActor._

class ControllerActor extends Actor with ActorLogging{

    private var view : ActorRef = _
    private var environment : ActorRef = _

    override def receive: Receive = {
        case Init =>
            view = context actorOf(ViewActor props, "view")
            environment = context actorOf(EnvironmentActor props, "environment")
            context become idle
        case _ => ???

    }

    private def idle: Receive = {
        case Start =>
            environment ! EnvironmentActor.Start(5, Constants.radius)
            context become running
        case Generate(n) =>
            environment ! EnvironmentActor.Generate(n)
        case Add(x, y) =>
            environment ! EnvironmentActor.Add(x, y)
        case Remove(x, y) =>
            environment ! EnvironmentActor.Remove(x, y)
        case _ => ???

    }

    private def running: Receive = {
        case Add(x, y) =>
            environment ! EnvironmentActor.Add(x, y)
        case Remove(x, y) =>
            environment ! EnvironmentActor.Remove(x, y)
        case Step =>
            environment ! EnvironmentActor.Step
        case Stop =>
            environment ! EnvironmentActor.Stop
            context become idle
        case _ => ???
    }
}

object ControllerActor {
    def props = Props(new ControllerActor())

    case class Generate(numberOfParticles: Int)
    case class Add(x: Int, y: Int)
    case class Remove(x: Int, y:Int)

    case object Start
    case object Pause
    case object Resume
    case object Stop
    case object Step
    case object Init
}

object Constants {
    val radius : Double = 100.0
}
