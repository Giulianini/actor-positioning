package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.sun.javafx.application.PlatformImpl
import it.unibo.pcd1819.actorpositioning.actors.ViewActor.Publish
import it.unibo.pcd1819.actorpositioning.model.Particle
import it.unibo.pcd1819.actorpositioning.view.screens.{ActorObserver, MainScreenView}
import it.unibo.pcd1819.actorpositioning.view.screens.ViewToActorMessages._

class ViewActor(var defaultParticles: Int,
                var defaultIterations: Int,
                var defaultTimeStep: Int,
                var logicSize: Double
               ) extends Actor with ActorLogging {
  PlatformImpl.startup(() => {})
  private val screenView: ActorObserver = MainScreenView(defaultParticles, defaultIterations, defaultTimeStep, logicSize)
  screenView.setViewActorRef(self)

  override def receive: Receive = {
    case Publish(e) => screenView.displayParticles(e)

    case StartSimulation => context.parent ! ControllerFSM.Start
    case PauseSimulation => context.parent ! ControllerFSM.Pause
    case StopSimulation => context.parent ! ControllerFSM.Stop
    case StepSimulation => context.parent ! ControllerFSM.Step
    case PrepareSimulation => context.parent ! ControllerFSM.GenerateEnvironment
    case SetParticle(amount: Int) => context.parent ! ControllerFSM.UpdateParticles(amount)
    case SetIteration(amount: Int) => context.parent ! ControllerFSM.UpdateIterations(amount)
    case SetTime(amount: Int) => context.parent ! ControllerFSM.UpdateTimeStep(amount)
    case AddParticle(posX: Double, posY: Double) => context.parent ! ControllerFSM.Add(posX, posY)
    case RemoveParticle(id: Int) => context.parent ! ControllerFSM.Remove(id)
    case Log(message: String) => log debug message
    case _ => log debug "View has been erroneously contacted"
  }
}

object ViewActor {
  def props(defaultParticles: Int, defaultIterations: Int, defaultTimeStep: Int, logicSize: Double): Props =
    Props(new ViewActor(defaultParticles, defaultIterations, defaultTimeStep, logicSize))
  final case class Publish(env: Seq[Particle])
  final case object Stop
}