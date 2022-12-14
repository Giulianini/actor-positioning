package it.unibo.pcd1819.actorpositioning.view.screens

import akka.actor.ActorRef
import it.unibo.pcd1819.actorpositioning.model.{Particle, Vector2D}
import it.unibo.pcd1819.actorpositioning.view.FXMLScreens
import it.unibo.pcd1819.actorpositioning.view.screens.ViewToActorMessages._
import it.unibo.pcd1819.actorpositioning.view.shapes.ShapeId
import it.unibo.pcd1819.actorpositioning.view.utilities.{ParticleDrawingUtils, ViewUtilities}
import it.unibo.pcd1819.actorpositioning.view.utilities.ViewUtilities._
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage

import scala.collection.mutable

sealed trait ActorObserver {
  def updateAdd(e: Seq[Particle], p: Particle, elapsed: Long): Unit
  def updateParticlesPositions(particles: Seq[Particle], elapsed: Long): Unit
  def updateRemove(e: Seq[Particle], removedId: Int, elapsed: Long): Unit
  def displayParticles(particles: Seq[Particle]): Unit
  def displayParticle(particle: Particle): Unit
  def setViewActorRef(actorRef: ActorRef): Unit
}

protected final case class MainScreenView(private var defaultParticles: Int,
                                          private var defaultIterations: Int,
                                          private var defaultTimeStep: Int,
                                          private var logicSize: Double
                                         ) extends AbstractMainScreenView(
  defaultParticles, defaultIterations, defaultTimeStep, logicSize) with ActorObserver {
  val particleMap: mutable.Map[Int, ShapeId] = mutable.Map()
  private var viewActorRef: ActorRef = _
  Platform.runLater(() => this.mainBorder = ViewUtilities.loadFxml(this, FXMLScreens.HOME).asInstanceOf[AnchorPane])

  @FXML override def initialize(): Unit = {
    super.initialize()
    val stage = new Stage()
    val scene = new Scene(this.mainBorder)
    stage.setScene(scene)
    chargeSceneSheets(scene)
    stage.setOnCloseRequest(_ => {
      System.exit(0)
    })
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
  override def askToAddParticle(posX: Double, posY: Double): Unit = this.viewActorRef ! AddParticle(posX, posY)
  override def askToRemoveParticle(id: Int): Unit = this.viewActorRef ! RemoveParticle(id)

  // ##################### FROM ACTOR
  override def setViewActorRef(actorRef: ActorRef): Unit = this.viewActorRef = actorRef
  override def displayParticles(particles: Seq[Particle]): Unit = {
    Platform.runLater(() => {
      this.particleMap.clear()
      this.getParticles.getChildren.clear()
      particles.foreach(p => displayParticle(p))
    })
  }
  override def displayParticle(particle: Particle): Unit = {
//    log("Gui display: " + particle.id)
    Platform.runLater(() => {
      val shape: ShapeId = ParticleDrawingUtils.createParticleShapes(particle, this.comboBoxShape.getSelectionModel.getSelectedItem,
        Vector2D(this.stack3D.getWidth, this.stack3D.getHeight), this.comboBoxOptimize.getSelectionModel.getSelectedIndex, this.logicSize, particle.id)
      this.setRemoveParticleOnClick(shape)
      this.getParticles.getChildren.add(shape)
      this.particleMap += (particle.id -> shape)
    })
  }
  def removeParticle(id: Int): Unit = {
    this.getParticles.getChildren.remove(this.getParticles.getChildren.stream()
      .filter(p => p.asInstanceOf[ShapeId].id == id).findFirst().get())
    this.particleMap -= id
  }
  override def updateParticlesPositions(particles: Seq[Particle], elapsed: Long): Unit = {
    Platform.runLater(() => {
      particles.foreach(p => {
        this.particleMap.get(p.id).foreach(shape => {
          val posX: Double = (p.position.x / logicSize) * this.stack3D.getWidth * 0.5 + this.stack3D.getWidth * 0.5
          val posY: Double = (p.position.y / logicSize) * this.stack3D.getHeight * 0.5 + this.stack3D.getHeight * 0.5
          shape.setTranslateX(posX)
          shape.setTranslateY(posY)
        })
      })
    })
  }
  override def updateRemove(e: Seq[Particle], removedId: Int, elapsed: Long): Unit = Platform.runLater(() => this.removeParticle(removedId))
  override def updateAdd(e: Seq[Particle], p: Particle, elapsed: Long): Unit = displayParticle(p)
}

object MainScreenView {
  def apply(defaultParticles: Int,
            defaultIterations: Int,
            defaultTimeStep: Int,
            logicSize: Double
           ): MainScreenView = new MainScreenView(defaultParticles, defaultIterations, defaultTimeStep, logicSize)
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
  final case class AddParticle(x: Double, y: Double)
  final case class RemoveParticle(id: Int)
  final case class Log(message: String)
}