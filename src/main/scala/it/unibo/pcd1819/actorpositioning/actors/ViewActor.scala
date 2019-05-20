package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}

class ViewActor extends Actor with ActorLogging{
    override def receive: Receive = {
        case _ =>
            log debug "View has been contacted"
    }
}

object ViewActor {
    def props = Props(new ViewActor())

    //  case class Environment()
}