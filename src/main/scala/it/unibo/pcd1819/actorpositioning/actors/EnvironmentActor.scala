package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import it.unibo.pcd1819.actorpositioning.actors.EnvironmentActor._
import it.unibo.pcd1819.actorpositioning.model.Particle

class EnvironmentActor extends Actor with ActorLogging {

    var particles: Seq[ActorRef] = List()

    private def simulationBehaviour: Receive = {
        case Step =>
            //log debug "Received step command"
            this.particles foreach (_ ! ParticleActor.Step)
        case ParticleInfo(p, id) => this.particles foreach (_ ! ParticleActor.ParticleInfo(p, id))
        case Stop => context unbecome()
    }

    override def receive: Receive = {
        case Start(particleAmount, range) =>
            this.particles = 0 until particleAmount map (context actorOf ParticleActor.props(_))
            this.particles foreach (_ ! ParticleActor.Start(range, particleAmount))
            context become simulationBehaviour
    }
}

object EnvironmentActor {
    val basePath = "particle-master"

    case class Start(particles: Int, within: Double)
    case class Add(x: Int, y: Int)
    case class Remove(x: Int, y: Int)
    case class Generate(n: Int)
    case object Step
    case object Stop
    case class ParticleInfo(particle: Particle, id: Int)
    def props = Props(classOf[EnvironmentActor])
}
