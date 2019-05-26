package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.sun.javafx.application.PlatformImpl
import it.unibo.pcd1819.actorpositioning.actors.ViewActor.Publish
import it.unibo.pcd1819.actorpositioning.model.{Environment, Particle}
import it.unibo.pcd1819.actorpositioning.view.screens.ViewToActorMessages._
import it.unibo.pcd1819.actorpositioning.view.screens.{ActorObserver, MainScreenView}

class ViewActor extends Actor with ActorLogging {
  PlatformImpl.startup(() => {})
  private val screenView: ActorObserver = MainScreenView()
  screenView.setViewActorRef(self)

  override def receive: Receive = {
    case Publish(e) => screenView.displayParticles(e)

    case StartSimulation => context.parent ! ControllerFSM.Start
    case PauseSimulation => context.parent ! ControllerFSM.Pause
    case StopSimulation => context.parent ! ControllerFSM.Stop
    case StepSimulation => context.parent ! ControllerFSM.Step
    case PrepareSimulation => context.parent ! ControllerFSM.GenerateEnvironment
    case SetParticle(amount: Int) => log debug s"Set particles $amount"
    case SetIteration(amount: Int) => log debug s"Set iteration $amount"
    case SetTime(amount: Int) => log debug s"Set time $amount"
    case Log(message: String) => log debug message
    case _ => log debug "View has been erroneously contacted"
  }
}

object ViewActor {
  def props = Props(new ViewActor())
  final case class Publish(env: Seq[Particle])
  final case object Stop
}