package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import it.unibo.pcd1819.actorpositioning.actors.EnvironmentActor._
import it.unibo.pcd1819.actorpositioning.model.Particle

class EnvironmentActor extends Actor with ActorLogging {

    private var particles: Seq[Particle] = Seq()

    private def simulationBehaviour: Receive = {
        case _ =>
    }

    override def receive: Receive = {
        case Start =>
            log debug "Starting simulation..."
            context become simulationBehaviour
        case Generate(n, range) =>
        case Add(x, y) =>
        case Remove(id) =>
        case ParticleFactoryActor.NewParticles(ps) =>
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
    final case class WorkUpdate(particles: Seq[Particle])
    final case class Add(x: Double, y: Double) extends Input
    final case class Generate(n: Int, range: Double) extends Input //expecting Result(Environment
    final case class Remove(p: Particle) extends Input
    final case class SetTimeStep(dt: Double) extends Input
    final case class BulkAdd(s: Seq[(Double, Double)]) extends Input
    final case class BulkRemove(S: Seq[Particle]) extends Input
}
