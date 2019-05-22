package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import it.unibo.pcd1819.actorpositioning.actors.WorkerActor._
import it.unibo.pcd1819.actorpositioning.model.Particle

import scala.concurrent.{ExecutionContext, Future}
import akka.pattern.pipe

class WorkerActor(siblings: Int)(implicit val executionContext: ExecutionContext) extends Actor with ActorLogging {

    private var particles: Seq[Particle] = Seq()
    private var particleDataReceived = 0
    private var particleData: Seq[Particle] = Seq()
    private var siblingRefs: Seq[ActorRef] = Seq()

    override def receive: Receive = addRemoveBehaviour orElse {
        case Start =>
            log debug "Starting to work..."
            context become simulationBehaviour
    }

    private def simulationBehaviour: Receive = addRemoveBehaviour orElse {
        case Step =>
            this.siblingRefs foreach(_ ! ParticleData(this.particles, self.path.name))
        case ParticleData(ps, name) if self.path.name != name =>
            this.particleDataReceived += 1
            this.particleData = particleData ++ ps
            this.particleDataReceived match {
                case n if n == siblings =>
                    this.particleDataReceived = 0
                    Future {
                        val update = this.particles map { p =>
                            val newParticle: Particle = p.copy()(p.id)
                            this.particleData foreach { that =>
                                newParticle applyForceFrom that
                            }
                            newParticle
                        }
                        WorkUpdate(update)
                    } pipeTo self
                    context become updateBehaviour
            }
        case Stop => {
            log debug "Stopping..."
            context unbecome()
        }
    }

    private def updateBehaviour: Receive = {
        case WorkUpdate(ps) =>
            this.particles = ps
            this.particleData = Seq()
            context unbecome()
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

    private case class WorkUpdate(particles: Seq[Particle])

    def props(siblings: Int) = Props(classOf[WorkerActor], siblings)
}
