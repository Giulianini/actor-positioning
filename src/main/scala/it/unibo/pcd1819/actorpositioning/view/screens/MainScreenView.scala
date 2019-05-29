package it.unibo.pcd1819.actorpositioning.view.screens

import java.util

import akka.actor.ActorRef
import it.unibo.pcd1819.actorpositioning.model.{Particle, Vector2D}
import it.unibo.pcd1819.actorpositioning.view.FXMLScreens
import it.unibo.pcd1819.actorpositioning.view.screens.ViewToActorMessages._
import it.unibo.pcd1819.actorpositioning.view.shapes.ShapeId
import it.unibo.pcd1819.actorpositioning.view.utilities.{ParticleDrawingUtils, ViewUtilities}
import it.unibo.pcd1819.actorpositioning.view.utilities.ViewUtilities._
import javafx.application.Platform
import javafx.application.Platform.runLater
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage

import scala.collection.mutable

trait ActorObserver {
  def updateParticlesPositions(particlesPosition: util.List[Particle]): Unit
  def displayParticles(particles: Seq[Particle], elapsed: Long): Unit
  def displayParticle(particle: Particle): Unit
  def removeParticle(id: Int): Unit
  def updateExecutionTime(millis: Long): Unit
  def setViewActorRef(actorRef: ActorRef): Unit
}

protected final case class MainScreenView() extends AbstractMainScreenView with ActorObserver {
  private var viewActorRef: ActorRef = _
  Platform.runLater(() => this.mainBorder = ViewUtilities.loadFxml(this, FXMLScreens.HOME).asInstanceOf[AnchorPane])
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
  override def log(message: String): Unit = this.viewActorRef ! Log(message)
  override def startSimulation(): Unit = this.viewActorRef ! StartSimulation
  override def pauseSimulation(): Unit = this.viewActorRef ! PauseSimulation
  override def stopSimulation(): Unit = this.viewActorRef ! StopSimulation
  override def stepSimulation(): Unit = this.viewActorRef ! StepSimulation
  override def prepareSimulation(): Unit = this.viewActorRef ! PrepareSimulation
  override def setParticles(amount: Int): Unit = this.viewActorRef ! SetParticle(amount)
  override def setIteration(amount: Int): Unit = this.viewActorRef ! SetIteration(amount)
  override def setTime(amount: Int, sliderMin: Double, sliderMax: Double): Unit = this.viewActorRef ! SetTime(amount)

  var particleIndex = 0

  override def askToAddParticle(posX: Double, posY: Double): Unit = {
    log("Asked to add a particle with y = " + posY)
    this.displayParticle(Particle(Vector2D(posX, posY), 30, 30, particleIndex))
    particleIndex = particleIndex + 1
  }
  override def askToRemoveParticle(index: Int): Unit = this.removeParticle(index) //TODO SEND TO ACTOR
  // ##################### FROM ACTOR
  override def setViewActorRef(actorRef: ActorRef): Unit = this.viewActorRef = actorRef
  override def displayParticles(particles: Seq[Particle], elapsed: Long): Unit = {
    Platform.runLater(() => {
      this.initialParticles = new mutable.MutableList()
      this.getParticles.getChildren.clear()
      particles.foreach(p => displayParticle(p))
    })
  }
  override def displayParticle(particle: Particle): Unit = {
    val shape: ShapeId = ParticleDrawingUtils.createParticleShapes(particle, this.comboBoxShape.getSelectionModel.getSelectedItem,
      Vector2D(this.stack3D.getWidth, this.stack3D.getHeight), particle.id)
    log("Created particle with index: " + shape.id)
    this.setRemoveParticleOnClick(shape)
    this.getParticles.getChildren.add(shape)
  }
  override def removeParticle(id: Int): Unit = {
    this.getParticles.getChildren.remove(this.getParticles.getChildren.stream()
      .filter(p => p.asInstanceOf[ShapeId].id == id).findFirst().get())
    log("Removed particle with index: " + id)
  }
  override def updateParticlesPositions(particlesPosition: util.List[Particle]): Unit = this.getClass
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
  final case class Log(message: String)
}