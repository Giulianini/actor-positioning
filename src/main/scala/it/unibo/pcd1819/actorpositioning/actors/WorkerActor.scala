package it.unibo.pcd1819.actorpositioning.actors

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Stash}
import it.unibo.pcd1819.actorpositioning.actors.WorkerActor._
import it.unibo.pcd1819.actorpositioning.model.{Particle, Vector2D}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import akka.pattern.pipe
import com.typesafe.config.ConfigFactory

class WorkerActor(siblings: Int)(implicit val executionContext: ExecutionContext) extends Actor with ActorLogging with Stash {

    private var particles: Seq[Particle] = Seq()
    private var particleDataReceived = 0
    private var particleData: Seq[Particle] = Seq()

    override def receive: Receive = addRemoveBehaviour orElse {
        case Start =>
            log debug "Starting to work..."
            context become simulationBehaviour
    }

    private def simulationBehaviour: Receive = addRemoveBehaviour orElse {
        case Step =>
            context.actorSelection("../*") ! ParticleData(this.particles, self.path.name)
        case ParticleData(ps, name) if self.path.name != name =>
            this.particleDataReceived += 1
            this.particleData = this.particleData ++ ps
            this.particleDataReceived match {
                case n if n == siblings =>
                    this.particleDataReceived = 0
                    Future {
                        val update = this.particles.map(p => {
                            var newParticle = p
                            this.particles.filter(_.id != newParticle.id)
                                .foreach(that => {
                                    newParticle = newParticle applyForceFrom that
                                })
                            this.particleData.foreach(that => {
                                newParticle = newParticle applyForceFrom that
                            })
                            newParticle commitForce()
                        })
                        WorkUpdate(update)
                    } pipeTo self
                    context become (updateBehaviour, discardOld = false)
            }
        case LoadRequest =>
            sender() ! EnvironmentActor.LoadUpdate(this.particles.size)
        case Stop => {
            log debug "Stopping..."
            context unbecome()
        }
    }

    private def updateBehaviour: Receive = {
        case WorkUpdate(ps) =>
            log debug ps.toString()
            this.particles = ps
            this.particleData = Seq()
            context.parent ! EnvironmentActor.WorkUpdate(ps)
            unstashAll()
            context unbecome()
        case _ => stash()
    }

    private def addRemoveBehaviour: Receive = {
        case Add(p) =>
            this.particles = this.particles :+ p
        case SetBulk(ps) =>
            log debug s"Received ${ps.size} particles"
            this.particles = ps
        case Remove(id) =>
            this.particles = this.particles.filter(_.id == id)
    }

}

object WorkerActor {
    case object Start
    case object Step
    case object Stop
    final case class Add(particle: Particle)
    final case class SetBulk(particles: Seq[Particle])
    final case class Remove(particleId: Int)

    case object LoadRequest

    private final case class ParticleData(particles: Seq[Particle], actorName: String)
    private case class WorkUpdate(particles: Seq[Particle])

    def props(siblings: Int)(implicit executionContext: ExecutionContext) = Props(classOf[WorkerActor], siblings, executionContext)
}

object WorkerMain extends App {
    implicit val context: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

    val sys = ActorSystem("workers", ConfigFactory.parseString("""akka.loglevel = "DEBUG""""))
    val worker = sys actorOf WorkerActor.props(1)
    val worker2 = sys actorOf WorkerActor.props(1)

    worker ! Start
    worker2 ! Start

    worker ! Add(Particle(Vector2D(2, 2), 1, 1, 0))
    worker2 ! Add(Particle(Vector2D(5, 5), 1, 1, 1))

    worker ! Step
    worker2 ! Step

    Thread.sleep(1000)
    worker ! Step
    worker2 ! Step

    val p: Particle = Particle(Vector2D.zero, 1, 1, 2)

}