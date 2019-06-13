package it.unibo.pcd1819.actorpositioning.actors

import java.util.concurrent.Executors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}
import akka.pattern.pipe
import com.typesafe.config.ConfigFactory
import it.unibo.pcd1819.actorpositioning.actors.WorkerActor._
import it.unibo.pcd1819.actorpositioning.model.{Particle, Vector2D}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class WorkerActor(siblings: Int)(implicit val executionContext: ExecutionContext) extends Actor with ActorLogging with Stash {

  private var particles: Seq[Particle] = Seq()
  private var particleDataReceived = 0
  private var particleData: Seq[Particle] = Seq()

  private var timeStep: Double = DefaultConstants.DEFAULT_TIME_STEP

  override def receive: Receive = addRemoveBehaviour orElse setTimestepBehaviour orElse simulationBehaviour orElse
    updateBehaviour orElse {
    case Start(dt) =>
      log debug "Starting to work..."
      this.timeStep = dt
  }

  private def simulationBehaviour: Receive = {
    case Step =>
      context.actorSelection("../*") ! ParticleData(this.particles, self.path.name)
    case ParticleData(ps, name) if self.path.name != name =>
      this.particleDataReceived += 1
      this.particleData = this.particleData ++ ps

      def workUpdateFuture(particles: Seq[Particle], others: Seq[Particle]): Future[WorkUpdate] = Future {
        implicit val dt: Double = this.timeStep
        val update: Seq[Particle] = particles.map(p => {
          var newParticle = p.copy()
          particles.filter(_.id != newParticle.id)
            .foreach(that => {
              newParticle = newParticle applyForceFrom that
            })
          others.foreach(that => {
            newParticle = newParticle applyForceFrom that
          })
          newParticle commitForce()
        })
        WorkUpdate(update)
      }

      this.particleDataReceived match {
        case n if n == siblings =>
          val particlesCopy = this.particles map (e => e)
          val particleDataCopy = this.particleData map (e => e)
          workUpdateFuture(particlesCopy, particleDataCopy) pipeTo self
          this.particleDataReceived = 0
          this.particleData = Seq()
          context become(updateBehaviour, discardOld = false)
        case _ =>
      }
    case LoadRequest =>
      sender() ! EnvironmentActor.LoadUpdate(this.particles.size)
    case Stop =>
      log info "Stopping..."
      context unbecome()
  }

  private def updateBehaviour: Receive = {
    case WorkUpdate(ps) =>
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
      //            log debug s"Received ${ps.size} particles"
      this.particles = ps
    case Remove(id) =>
      this.particles = this.particles.filter(_.id != id)
  }

  private def setTimestepBehaviour: Receive = {
    case SetTimestep(dt) => this.timeStep = dt
  }

}

object WorkerActor {
  final case class Start(dt: Double)
  case object Step
  case object Stop
  final case class Add(particle: Particle)
  final case class SetBulk(particles: Seq[Particle])
  final case class Remove(particleId: Int)

  case object LoadRequest

  final case class SetTimestep(dt: Double)

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