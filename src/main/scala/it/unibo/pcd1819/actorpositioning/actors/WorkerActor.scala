package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import it.unibo.pcd1819.actorpositioning.actors.WorkerActor._
import it.unibo.pcd1819.actorpositioning.model.Particle

class WorkerActor(siblings: Int) extends Actor with ActorLogging {

    private var particles: Seq[Particle] = Seq()
    private var particleDataReceived = 0
    private var siblingRefs: Seq[ActorRef] = Seq()

    override def receive: Receive = addRemoveBehaviour orElse {
        case Start =>
            log debug "Starting to work..."
            context become simulationBehaviour
    }

    private def simulationBehaviour: Receive = addRemoveBehaviour orElse {
        case Step =>
            this.siblingRefs foreach(_ ! ParticleData(this.particles, self.path.name))
        case ParticleData(ps, name) if self.path.name != name => // TODO do computation
        case Stop => {
            log debug "Stopping..."
            context unbecome()
        }
    }

    private def addRemoveBehaviour: Receive = {
        case Add(p) =>
            this.particles = this.particles :+ p
        case Remove(id) =>
            this.particles = this.particles.filter(_.id == id)
    }

}

object WorkerActor {
    case object Start
    case object Step
    case object Stop
    final case class Add(particle: Particle)
    final case class Remove(particleId: Int)
    final case class ParticleData(particles: Seq[Particle], actorName: String)

    def props(siblings: Int) = Props(classOf[WorkerActor], siblings)
}
