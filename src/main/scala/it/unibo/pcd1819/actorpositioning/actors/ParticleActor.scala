package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}
import it.unibo.pcd1819.actorpositioning.model.Particle
import it.unibo.pcd1819.actorpositioning.actors.ParticleActor._

class ParticleActor(id: Int) extends Actor with ActorLogging {

    var particle: Particle = _
    var particleInfoCounter = 0
    var particleAmount = 0

    private def simulationBehaviour: Receive = {
        case Step =>
            //log debug "Received step command"
            context.actorSelection("../*") ! ParticleInfo(this.particle, this.id)
        case ParticleInfo(p, id) if id != this.id =>
            this.particle applyForceFrom p
            this.particleInfoCounter += 1
            this.particleInfoCounter match {
                case n if n == this.particleAmount - 1 =>
                    this.particleInfoCounter = 0
                    this.particle commitForce()
                    log debug particle.position.toString
                case _ =>
            }
        case Stop => context unbecome()
    }

    override def receive: Receive = {
        case Start(range, amount) =>
            this.particle = Particle.random(range)
            this.particleAmount = amount
            log debug particle.position.toString
            context become simulationBehaviour
    }
}

object ParticleActor {
    case class Start(within: Double, particleAmount: Int)
    case object Stop
    case object Step
    case class ParticleInfo(particle: Particle, id: Int)

    def props(id: Int) = Props(classOf[ParticleActor], id)
}
