package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.sun.javafx.application.PlatformImpl
import it.unibo.pcd1819.actorpositioning.actors.ViewActor.Publish
import it.unibo.pcd1819.actorpositioning.model.Environment
import it.unibo.pcd1819.actorpositioning.view.screens.ViewToActorMessages.{PauseSimulation, PrepareSimulation, SetIteration, SetParticle, SetTime, StartSimulation, StepSimulation, StopSimulation}
import it.unibo.pcd1819.actorpositioning.view.screens.{ActorObserver, MainScreenView}

private class ViewActor extends Actor with ActorLogging {
  PlatformImpl.startup(() => {})
  private val screenView: ActorObserver = MainScreenView()
  screenView.setViewActorRef(self)

  override def receive: Receive = {
    case Publish(e) => log debug s"$e"
    case StartSimulation => log debug "Start the simulation"
    case PauseSimulation => log debug "Pause the simulation"
    case StopSimulation => log debug "Stop the simulation"
    case StepSimulation => log debug "Step the simulation"
    case PrepareSimulation => log debug "Prepare the simulation"
    case SetParticle(amount: Int) => log debug s"Set particles $amount"
    case SetIteration(amount: Int) => log debug s"Set iteration $amount"
    case SetTime(amount: Int) => log debug s"Set time $amount"
    case _ => log debug "View has been erroneously contacted"
  }
}

object ViewActor {
  def props = Props(new ViewActor())

  final case class Publish(env: Environment)

}