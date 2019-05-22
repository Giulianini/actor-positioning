package it.unibo.pcd1819.actorpositioning.view.screens
import java.util

import com.jfoenix.controls.{JFXButton, JFXPopup, JFXSlider, JFXToolbar}
import com.sun.javafx.application.PlatformImpl
import it.unibo.pcd1819.actorpositioning.model.Particle
import it.unibo.pcd1819.actorpositioning.view.{HOME, POPUP_GUI}
import it.unibo.pcd1819.actorpositioning.view.utilities.{BIG_ICON, INFO_NOTIFICATION, LONG_DURATION, ViewUtilities}
import javafx.application.Platform
import javafx.application.Platform.runLater
import javafx.fxml.FXML
import javafx.scene.{Camera, Group, PerspectiveCamera, Scene, SceneAntialiasing, SubScene}
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.layout.{AnchorPane, BorderPane, StackPane}
import javafx.stage.Stage
import org.apache.log4j.Logger
import org.kordamp.ikonli.material.Material

trait ControllerObserver {
  def updateParticlesPositions(particlesPosition: util.List[Particle]): Unit
  def displayParticles(particles: util.List[Particle]): Unit
  def updateExecutionTime(millis: Long): Unit
}

trait View {
  def startSimulation(): Unit
  def stopSimulation(): Unit
  def prepareSimulation(): Unit
}


abstract class  AbstractMainScreenView() extends View {
  private var popupScreenView: PopupScreenView = new PopupScreenView()
  private var popup: JFXPopup = _
  private val startIcon = ViewUtilities.iconSetter(Material.PLAY_ARROW, BIG_ICON)
  private val stopIcon = ViewUtilities.iconSetter(Material.STOP, BIG_ICON)
  private var camera: Camera = _
  private var particles: Group = _
  @FXML private var mainBorder: AnchorPane = _
  @FXML private var toolbar: JFXToolbar = _
  @FXML private var stack3D: StackPane = _
  @FXML private var buttonCreateParticles: JFXButton = _
  @FXML private var buttonPopup: JFXButton = _
  @FXML private var buttonStartStop: JFXButton = _
  @FXML private var labelExecutionTime: Label = _
  //@FXML private var comboBoxShape: JFXComboBox[String] = _

  @FXML def initialize(): Unit = {
    this.assertNodeInjected()
    this.preparePopup()
    this.prepareButtons()
    this.prepareScene3D()
    this.prepareHideToolbar()
    this.showPopupInfo()
  }

  private def showPopupInfo(): Unit = {
    ViewUtilities.showNotificationPopup("Help", "Click '^' and create a configuration \nRight Click on screen hides toolbar", LONG_DURATION, INFO_NOTIFICATION, null)
  }

  private def assertNodeInjected(): Unit = {
    assert(this.mainBorder != null, "fx:id=\"mainBorderPopup\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.stack3D != null, "fx:id=\"stack3D\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.toolbar != null, "fx:id=\"toolbar\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.buttonPopup != null, "fx:id=\"buttonPopup\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.labelExecutionTime != null, "fx:id=\"labelExecutionTime\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.buttonStartStop != null, "fx:id=\"buttonStartStop\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.buttonCreateParticles != null, "fx:id=\"buttonCreateParticles\" was not injected: check your FXML file 'PopupScreen.fxml'.")
  }

  private def preparePopup(): Unit = {
    this.popup = new JFXPopup(this.popupScreenView.mainBorderPopup)
  }

  private def prepareButtons(): Unit = {
    this.buttonStartStop.setGraphic(this.startIcon)
    this.buttonPopup.setGraphic(ViewUtilities.iconSetter(Material.ARROW_DROP_DOWN, BIG_ICON))
    this.buttonStartStop.setOnAction(_ => {
       this.buttonStartStop.getGraphic match {
        case this.startIcon =>  this.buttonStartStop.setGraphic(this.stopIcon)
          this.buttonPopup.setDisable(true)
          this.startSimulation()
        case this.stopIcon =>
          this.buttonPopup.setDisable(false)
          this.buttonStartStop.setGraphic(this.startIcon)
          this.stopSimulation()
      }
    })
    this.buttonPopup.setOnAction(_ => this.popup.show(mainBorder))
    this.buttonCreateParticles.setGraphic(ViewUtilities.iconSetter(Material.BUBBLE_CHART, BIG_ICON))
    this.buttonCreateParticles.setOnAction(_ => this.prepareSimulation())
  }

  private def prepareScene3D(): Unit = {
    this.particles = new Group
    val scene3D = new SubScene(this.particles, 100, 100, true, SceneAntialiasing.BALANCED)
    this.stack3D.getChildren.add(scene3D)
    this.camera = new PerspectiveCamera
    scene3D.setCamera(this.camera)
    scene3D.setRoot(this.particles)
    scene3D.widthProperty.bind(this.stack3D.widthProperty)
    scene3D.heightProperty.bind(this.stack3D.heightProperty)
  }

  private def prepareHideToolbar(): Unit = {
    this.mainBorder.setOnMouseClicked(ev => {
      if (ev.getButton == MouseButton.SECONDARY && this.toolbar.isVisible) {
        this.toolbar.setVisible(false)
      } else if (ev.getButton == MouseButton.SECONDARY && !this.toolbar.isVisible) {
        this.toolbar.setVisible(true)
      }
    })
  }

  override def startSimulation(): Unit
  override def stopSimulation(): Unit
  override def prepareSimulation(): Unit
  def setParticles(amount: Int): Unit
  def setIteration(amount: Int): Unit
  def setTime(amount: Int, sliderMin: Double, sliderMax: Double): Unit

  class PopupScreenView {
    @FXML var mainBorderPopup: BorderPane = _
    @FXML var sliderParticles: JFXSlider = _
    @FXML var sliderIteration: JFXSlider = _
    @FXML var sliderTimeStep: JFXSlider = _
    this.mainBorderPopup = ViewUtilities.loadFxml(this, POPUP_GUI).asInstanceOf[BorderPane]

    @FXML private[screens] def initialize(): Unit = {
      this.assertNodeInjected()
      this.prepareSliders()
    }

    private def assertNodeInjected(): Unit = {
      assert(this.sliderParticles != null, "fx:id=\"sliderParticles\" was not injected: check your FXML file 'PopupScreen.fxml'.")
      assert(this.sliderIteration != null, "fx:id=\"sliderIteration\" was not injected: check your FXML file 'PopupScreen.fxml'.")
      assert(this.sliderTimeStep != null, "fx:id=\"sliderTimeStep\" was not injected: check your FXML file 'PopupScreen.fxml'.")
    }

    private def prepareSliders(): Unit = {
      this.sliderParticles.setOnMouseReleased(_ => AbstractMainScreenView.this.setParticles(this.sliderParticles.getValue.toInt))
      this.sliderIteration.setOnMouseReleased(_ => AbstractMainScreenView.this.setIteration(this.sliderIteration.getValue.toInt))
      this.sliderTimeStep.setOnMouseReleased(_ => AbstractMainScreenView.this.setTime(this.sliderTimeStep.getValue.toInt, this.sliderTimeStep.getMin, this.sliderTimeStep.getMax))
    }
  }
}

object MainScreenViewImpl extends AbstractMainScreenView with ControllerObserver {
  val LOG: Logger = Logger.getLogger(MainScreenViewImpl.getClass)
  @FXML private var mainBorder: AnchorPane = _
  @FXML private val labelExecutionTime: Label = null

  mainBorder = ViewUtilities.loadFxml(this, HOME).asInstanceOf[AnchorPane]

  @FXML override def initialize(): Unit = {
    super.initialize()
    val stage = new Stage
    val scene = new Scene(this.mainBorder)
    stage.setScene(scene)
    ViewUtilities.chargeSceneSheets(scene)
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
  override def updateParticlesPositions(particlesPosition: util.List[Particle]): Unit = LOG.debug("UPDATE PARTICLES")
  override def displayParticles(particles: util.List[Particle]): Unit = LOG.debug("DISPLAY PARTICLE")
  override def updateExecutionTime(millis: Long): Unit = runLater(() => labelExecutionTime.setText(millis + " "))
}

object Main extends App {
  PlatformImpl.startup(() => {})
  Platform.runLater(() => MainScreenViewImpl)
}
