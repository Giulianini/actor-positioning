package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}

class ViewActor extends Actor with ActorLogging{
    override def receive: Receive = ???
}

object ViewActor {
    def props = Props(new ViewActor())

    //  case class Environment()
}