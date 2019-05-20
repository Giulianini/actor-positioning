package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}

class EnvironmentActor(private var numberOfParticles: Int) extends Actor with ActorLogging{
  override def receive: Receive = ???
}

object EnvironmentActor {
  def props(numberOfParticles: Int) = Props(new EnvironmentActor(numberOfParticles))

  case class Start(numberOfParticles: Int)
  case class Add(x: Int, y: Int)
  case class Remove(x: Int, y:Int)

  case object Step
}
