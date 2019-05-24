package it.unibo.pcd1819.actorpositioning.view.screens

import java.util

import akka.actor.ActorRef
import it.unibo.pcd1819.actorpositioning.model.{Particle, Vector2D}
import it.unibo.pcd1819.actorpositioning.view.FXMLScreens
import it.unibo.pcd1819.actorpositioning.view.screens.ViewToActorMessages.{PauseSimulation, PrepareSimulation, SetIteration, SetParticle, SetTime, StartSimulation, StepSimulation, StopSimulation}
import it.unibo.pcd1819.actorpositioning.view.utilities.JavafxEnums.RECTANGULAR
import it.unibo.pcd1819.actorpositioning.view.utilities.{ParticleDrawingUtils, ViewUtilities}
import it.unibo.pcd1819.actorpositioning.view.utilities.ViewUtilities._
import javafx.application.Platform
import javafx.application.Platform.runLater
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import org.apache.log4j.Logger

import scala.collection.mutable

trait ActorObserver {
  def updateParticlesPositions(particlesPosition: util.List[Particle]): Unit
  def displayParticles(particles: Seq[Particle]): Unit
  def updateExecutionTime(millis: Long): Unit
  def setViewActorRef(actorRef: ActorRef): Unit
}

protected final case class MainScreenView() extends AbstractMainScreenView with ActorObserver {
  private var viewActorRef: ActorRef = _

  Platform.runLater(() => this.mainBorder = ViewUtilities.loadFxml(this, FXMLScreens.HOME).asInstanceOf[AnchorPane])
  private val LOG: Logger = Logger.getLogger(MainScreenView.getClass)
  private var initialParticles: Seq[Particle] = _

  @FXML override def initialize(): Unit = {
    super.initialize()
    val stage = new Stage()
    val scene = new Scene(this.mainBorder)
    stage.setScene(scene)
    chargeSceneSheets(scene)
    stage.show()
  }

  // ##################### TO ACTOR
  override def startSimulation(): Unit = this.viewActorRef ! StartSimulation
  override def pauseSimulation(): Unit = this.viewActorRef ! PauseSimulation
  override def stopSimulation(): Unit = this.viewActorRef ! StopSimulation
  override def stepSimulation(): Unit = this.viewActorRef ! StepSimulation
  override def prepareSimulation(): Unit = this.viewActorRef ! PrepareSimulation
  override def setParticles(amount: Int): Unit = this.viewActorRef ! SetParticle(amount)
  override def setIteration(amount: Int): Unit = this.viewActorRef ! SetIteration(amount)
  override def setTime(amount: Int, sliderMin: Double, sliderMax: Double): Unit = this.viewActorRef ! SetTime(amount)

  // ##################### FROM ACTOR
  override def setViewActorRef(actorRef: ActorRef): Unit = this.viewActorRef = actorRef
  override def displayParticles(particles: Seq[Particle]): Unit = {
    Platform.runLater(() => {
      this.initialParticles = new mutable.MutableList()
      this.getParticles.getChildren.clear()
      particles.foreach(p =>this.getParticles.getChildren.add(ParticleDrawingUtils.
          createParticleShapes(p, RECTANGULAR, Vector2D(this.mainBorder.getWidth, this.mainBorder.getHeight))))
    })
  }
  override def updateParticlesPositions(particlesPosition: util.List[Particle]): Unit = LOG.debug("UPDATE PARTICLES")
  override def updateExecutionTime(millis: Long): Unit = runLater(() => labelExecutionTime.setText(millis + " "))
}

object MainScreenView {
  def apply(): MainScreenView = new MainScreenView()
}

object ViewToActorMessages {
  final case object StartSimulation
  final case object PauseSimulation
  final case object StopSimulation
  final case object StepSimulation
  final case object PrepareSimulation
  final case class SetParticle(amount: Int)
  final case class SetIteration(amount: Int)
  final case class SetTime(amount: Int)
}