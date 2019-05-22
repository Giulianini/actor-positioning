package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import it.unibo.pcd1819.actorpositioning.actors.EnvironmentActor._
import it.unibo.pcd1819.actorpositioning.model.Particle

class EnvironmentActor extends Actor with ActorLogging {

    var particles: Seq[ActorRef] = List()

    private def simulationBehaviour: Receive = {
        case _ =>
    }

    override def receive: Receive = {
        case _ =>
    }
}

object EnvironmentActor {
    val basePath = "particle-master"

    case class Start(particles: Int, within: Double)
    case class ParticleInfo(particle: Particle, id: Int)

    def props = Props(classOf[EnvironmentActor])

    sealed trait Input
    case object Start extends Input
    case object Step extends Input //expecting Result(Environment)
    case object Stop extends Input
    final case class Generate(n: Int, within: Double) extends Input //expecting Result(Environment
    final case class Add(x: Int, y: Int) extends Input
    final case class Remove(p: Particle) extends Input
    final case class BulkAdd(s: Seq[(Int, Int)]) extends Input
    final case class BulkRemove(S: Seq[Particle]) extends Input
}
