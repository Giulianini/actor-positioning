package it.unibo.pcd1819.actorpositioning.view.screens

import java.util
import akka.actor.ActorRef
import com.sun.javafx.application.PlatformImpl
import it.unibo.pcd1819.actorpositioning.model.Particle
import it.unibo.pcd1819.actorpositioning.view.FXMLScreens
import it.unibo.pcd1819.actorpositioning.view.utilities.ViewUtilities._
import javafx.application.Platform
import javafx.application.Platform.runLater
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import org.apache.log4j.Logger

trait ControllerObserver {
  def updateParticlesPositions(particlesPosition: util.List[Particle]): Unit
  def displayParticles(particles: util.List[Particle]): Unit
  def updateExecutionTime(millis: Long): Unit
  def setViewActorRef(actorRef: ActorRef): Unit
}

object MainScreenViewImpl extends AbstractMainScreenView with ControllerObserver {
  private var viewActorRef: ActorRef = _
  val LOG: Logger = Logger.getLogger(MainScreenViewImpl.getClass)

  mainBorder = loadFxml(this, FXMLScreens.HOME).asInstanceOf[AnchorPane]

  @FXML override def initialize(): Unit = {
    super.initialize()
    val stage = new Stage
    val scene = new Scene(this.mainBorder)
    stage.setScene(scene)
    chargeSceneSheets(scene)
    stage.show()
  }


  // ##################### TO CONTROLLER
  override def startSimulation(): Unit = LOG.debug("START")
  override def stopSimulation(): Unit = LOG.debug("STOP")
  override def prepareSimulation(): Unit = LOG.debug("PREPARE")
  override def setParticles(amount: Int): Unit = LOG.debug("SET PARTICLE")
  override def setIteration(amount: Int): Unit = LOG.debug("SET ITERATION")
  override def setTime(amount: Int, sliderMin: Double, sliderMax: Double): Unit = LOG.debug("SET TIME")

  // ##################### FROM CONTROLLER
  override def setViewActorRef(actorRef: ActorRef): Unit = this.viewActorRef = actorRef
  override def updateParticlesPositions(particlesPosition: util.List[Particle]): Unit = LOG.debug("UPDATE PARTICLES")
  override def displayParticles(particles: util.List[Particle]): Unit = LOG.debug("DISPLAY PARTICLE")
  override def updateExecutionTime(millis: Long): Unit = runLater(() => labelExecutionTime.setText(millis + " "))
}

object Main extends App {
  PlatformImpl.startup(() => {})
  Platform.runLater(() => MainScreenViewImpl)
}
