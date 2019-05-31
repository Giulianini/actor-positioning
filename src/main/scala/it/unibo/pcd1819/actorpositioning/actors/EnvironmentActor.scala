package it.unibo.pcd1819.actorpositioning.actors

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import it.unibo.pcd1819.actorpositioning.actors.EnvironmentActor._
import it.unibo.pcd1819.actorpositioning.model.Particle

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

class EnvironmentActor extends Actor with ActorLogging with Stash {

  private var startingParticles: Seq[Particle] = Seq()
  private val particleFactory: ActorRef = context actorOf(ParticleFactoryActor.props, ParticleFactoryActor.name)

  private var timeStep = DefaultConstants.DEFAULT_TIME_STEP

  private val processors = Runtime.getRuntime.availableProcessors() + 1
  private implicit val executor: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(processors))
  private var workers: Seq[ActorRef] = 0 until processors map (_ => context actorOf WorkerActor.props(processors - 1))

  private var updatesReceived = 0
  private var updatedParticles: Seq[Particle] = Seq()

  private var toBeAdded: (Double, Double) = _
  private var minimumLoad = Int.MaxValue
  private var minimumLoadActor: ActorRef = _
  private var loadReplies = 0
  private var discoveringLoads = false

  override def receive: Receive = {
    //        case Start =>
    //            log debug "Starting simulation..."
    //            workers = 0 until processors map (_ => context actorOf WorkerActor.props(processors - 1))
    //            workers foreach (_ ! WorkerActor.Start(this.timeStep))
    //            val workerLoad = Math.ceil(this.startingParticles.size.toDouble / workers.size).toInt
    //            log debug "load: " + workerLoad
    //            log debug "workers: " + workers.size
    //            this.startingParticles.indices
    //                .zip(this.startingParticles)
    //                .groupBy {
    //                    case (i, _) => i / workerLoad
    //                }
    //                .map {
    //                    case (i, l) => (i, l.map { case (_, p) => p })
    //                }
    //                .map {
    //                    case (_, ps) => ps
    //                }
    //                .map(ps => {log debug "chunk size: " + ps.size ; ps})
    //                .zip(this.workers)
    //                .foreach {
    //                    case (ps, w) => w ! WorkerActor.SetBulk(ps)
    //                }
    case Stop =>
      //            log debug "Stopping simulation..."
      workers foreach context.stop
      context unbecome()
    case Step => {
      //            log debug "Stepping simulation"
      this.workers foreach (_ ! WorkerActor.Step)
    }
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
    case WorkUpdate(ps) =>
      this.updatesReceived += 1
      this.updatedParticles = this.updatedParticles ++ ps
      this.updatesReceived match {
        case n if n == workers.size =>
          //log info this.updatedParticles.map(p => p.id).sorted.toString()
          context.parent ! ControllerFSM.Result(this.updatedParticles)
          this.updatesReceived = 0
          this.updatedParticles = Seq()
        case _ =>
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
          val future: Future[Any] = this.particleFactory ? ParticleFactoryActor.CreateParticle(this.toBeAdded._1, this.toBeAdded._2)
          future.map {
            case ParticleFactoryActor.NewParticle(p) =>
              AddToWorker(p, this.minimumLoadActor)
            case _ =>
              log debug "asjdkl"
          } pipeTo self

          unstashAll()
        case _ =>
      }
    case AddToWorker(p, a) =>
      this.startingParticles = this.startingParticles :+ p
      a ! WorkerActor.Add(p)
      context.parent ! ControllerFSM.ResultAdded(this.startingParticles, p)
    case Remove(id) =>
      log info "Remove: " + id
      this.workers foreach (_ ! WorkerActor.Remove(id))
      this.startingParticles = this.startingParticles.filter(_.id != id)
      context.parent ! ControllerFSM.ResultRemoved(this.startingParticles, id)
    case ParticleFactoryActor.NewParticles(ps) =>
      //            log debug "Received particles"
      this.startingParticles = ps
      val workerLoad = Math.ceil(this.startingParticles.size.toDouble / workers.size).toInt
      log debug "load: " + workerLoad
      log debug "workers: " + workers.size
      this.startingParticles.indices
        .zip(this.startingParticles)
        .groupBy {
          case (i, _) => i / workerLoad
        }
        .map {
          case (i, l) => (i, l.map { case (_, p) => p })
        }
        .map {
          case (_, ps) => ps
        }
        .map(ps => {
          log debug "chunk size: " + ps.size;
          ps
        })
        .zip(this.workers)
        .foreach {
          case (ps, w) => w ! WorkerActor.SetBulk(ps)
        }
      context.parent ! ControllerFSM.Result(ps)
    case SetTimeStep(dt) => this.workers foreach (_ ! SetTimeStep(dt))
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
  final case class Remove(id: Int) extends Input
  final case class SetTimeStep(dt: Double) extends Input

  final case class LoadUpdate(particles: Int)
  final case class AddToWorker(particle: Particle, actorRef: ActorRef)
}
