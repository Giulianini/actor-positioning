package it.unibo.pcd1819.actorpositioning.actors

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import it.unibo.pcd1819.actorpositioning.actors.EnvironmentActor._
import it.unibo.pcd1819.actorpositioning.model.Particle

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration._

class EnvironmentActor extends Actor with ActorLogging with Stash {

    private var startingParticles: Seq[Particle] = Seq()
    private var particles: Seq[Particle] = Seq()
    private val particleFactory: ActorRef = context actorOf (ParticleFactoryActor.props, ParticleFactoryActor.name)

    private var workers: Seq[ActorRef] = Seq()

    private var toBeAdded: (Double, Double) = _
    private var minimumLoad = Int.MaxValue
    private var minimumLoadActor: ActorRef = _
    private var loadReplies = 0
    private var discoveringLoads = false

    private val processors = Runtime.getRuntime.availableProcessors() + 1
    private implicit val executor: ExecutionContextExecutor =
        ExecutionContext.fromExecutor(Executors.newFixedThreadPool(processors))

    private def simulationBehaviour: Receive = {
        case Stop =>
            log debug "Stopping simulation..."
            workers foreach context.stop
            context unbecome()
    }

    override def receive: Receive = {
        case Start =>
            log debug "Starting simulation..."
            workers = 0 to processors map (_ => context actorOf WorkerActor.props(processors))
            workers foreach (_ ! WorkerActor.Start)
            val chunks: Seq[Seq[Particle]] = (this.startingParticles grouped (this.startingParticles.size / processors)).toSeq
            log debug s"dim ${chunks.size}"
            chunks.indices foreach (i => workers(i) ! WorkerActor.SetBulk(chunks(i)))
            context become simulationBehaviour
        case Generate(n, range) =>
            particleFactory ! ParticleFactoryActor.GenerateRandomParticles(n, range)
        case Add(x, y) =>
            this.toBeAdded = (x, y)
            if (this.discoveringLoads) {
                stash()
            } else {
                this.discoveringLoads = true
                this.workers foreach (_ ! WorkerActor.LoadRequest)
            }
        case LoadUpdate(load) =>
            this.loadReplies += 1
            if (load < this.minimumLoad) {
                this.minimumLoad = load
                this.minimumLoadActor = sender()
            }
            this.loadReplies match {
                case n if n == this.workers.size =>
                    log debug s"Found minimum load actor with size ${this.minimumLoad}"
                    this.loadReplies = 0
                    this.discoveringLoads = false
                    this.minimumLoad = Int.MaxValue

                    implicit val timeout: Timeout = Timeout(5.seconds)
                    (this.particleFactory ? ParticleFactoryActor.CreateParticle(this.toBeAdded._1, this.toBeAdded._2)).map {
                        case ParticleFactoryActor.NewParticle(p) => AddToWorker(p, this.minimumLoadActor)
                    } pipeTo self

                    unstashAll()
                case _ =>
            }
        case AddToWorker(p, a) =>
            this.startingParticles = this.startingParticles :+ p
            a ! WorkerActor.Add(p)
        case Remove(id) =>
        case ParticleFactoryActor.NewParticles(ps) =>
            log debug "Received particles"
            this.startingParticles = ps
            context.parent ! ControllerFSM.Result(ps)
        case SetTimeStep(dt) =>
    }
}

object EnvironmentActor {
    val name = "environment"

    case class Start(particles: Int, within: Double)
    case class ParticleInfo(particle: Particle, id: Int)

    def props = Props(classOf[EnvironmentActor])

    sealed trait Input
    case object Start extends Input
    case object Step extends Input
    case object Stop extends Input
    final case class WorkUpdate(particles: Seq[Particle])
    final case class Add(x: Double, y: Double) extends Input
    final case class Generate(n: Int, range: Double) extends Input
    final case class Remove(p: Particle) extends Input
    final case class SetTimeStep(dt: Double) extends Input

    final case class LoadUpdate(particles: Int)
    final case class AddToWorker(particle: Particle, actorRef: ActorRef)
}
