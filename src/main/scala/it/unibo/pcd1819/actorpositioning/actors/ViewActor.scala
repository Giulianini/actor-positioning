package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}
import it.unibo.pcd1819.actorpositioning.actors.ViewActor.Publish
import it.unibo.pcd1819.actorpositioning.model.Environment

class ViewActor extends Actor with ActorLogging{
    override def receive: Receive = {
        case Publish(e) => log debug s"$e"
        case _ => log debug "View has been erroneously contacted"
    }
}

object ViewActor {
    def props = Props(new ViewActor())

//    final case class Publish(e: Seq[(Int, Int)])
    final case class Publish(env: Environment)
}