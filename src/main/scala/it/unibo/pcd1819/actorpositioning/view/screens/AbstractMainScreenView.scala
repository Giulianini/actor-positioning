package it.unibo.pcd1819.actorpositioning.view.screens
import com.jfoenix.controls.{JFXButton, JFXComboBox, JFXPopup, JFXSlider, JFXToolbar}
import it.unibo.pcd1819.actorpositioning.view.FXMLScreens.POPUP_GUI
import it.unibo.pcd1819.actorpositioning.view.utilities.{JavafxEnums, ViewUtilities}
import javafx.fxml.FXML
import javafx.scene.{Camera, Group, PerspectiveCamera, SceneAntialiasing, SubScene}
import javafx.scene.control.Label
import javafx.scene.input.MouseButton
import javafx.scene.layout.{AnchorPane, BorderPane, StackPane}
import org.kordamp.ikonli.material.Material

protected trait View {
  def startSimulation(): Unit
  def pauseSimulation(): Unit
  def stopSimulation(): Unit
  def prepareSimulation(): Unit
}

protected abstract class  AbstractMainScreenView extends View{
  private val popupScreenView: PopupScreenView = new PopupScreenView
  private val startIcon = ViewUtilities iconSetter(Material.PLAY_ARROW, JavafxEnums.BIG_ICON)
  private val pauseIcon = ViewUtilities iconSetter(Material.PAUSE, JavafxEnums.BIG_ICON)
  private var popup: JFXPopup = _
  private var camera: Camera = _
  private var particles: Group = _
  @FXML protected var mainBorder: AnchorPane = _
  @FXML protected var toolbar: JFXToolbar = _
  @FXML protected var stack3D: StackPane = _
  @FXML protected var buttonCreateParticles: JFXButton = _
  @FXML protected var buttonPopup: JFXButton = _
  @FXML protected var buttonStep: JFXButton = _
  @FXML protected var buttonStartPause:JFXButton = _
  @FXML protected var buttonStop: JFXButton = _
  @FXML protected var labelExecutionTime: Label = _
  @FXML protected var comboBoxShape: JFXComboBox[String] = _

  @FXML def initialize(): Unit = {
    this.assertNodeInjected()
    this.preparePopup()
    this.prepareButtons()
    this.prepareScene3D()
    this.prepareHideToolbar()
    this.showPopupInfo()
  }

  private def showPopupInfo(): Unit = {
    ViewUtilities.showNotificationPopup("Help", "Click '^' and create a configuration \nRight Click on screen hides toolbar",
      JavafxEnums.LONG_DURATION, JavafxEnums.INFO_NOTIFICATION, null)
  }

  private def assertNodeInjected(): Unit = {
    assert(this.mainBorder != null, "fx:id=\"mainBorderPopup\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.stack3D != null, "fx:id=\"stack3D\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.toolbar != null, "fx:id=\"toolbar\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.buttonPopup != null, "fx:id=\"buttonPopup\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.labelExecutionTime != null, "fx:id=\"labelExecutionTime\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.buttonStartPause != null, "fx:id=\"buttonStartPause\" was not injected: check your FXML file 'MainScreen.fxml'.")
    assert(this.buttonCreateParticles != null, "fx:id=\"buttonCreateParticles\" was not injected: check your FXML file 'PopupScreen.fxml'.")
  }

  private def preparePopup(): Unit = {
    this.popup = new JFXPopup(this.popupScreenView mainBorder())
  }

  private def prepareButtons(): Unit = {
    this.buttonStartPause.setGraphic(this.startIcon)
    this.buttonPopup.setGraphic(ViewUtilities iconSetter(Material.ARROW_DROP_DOWN, JavafxEnums.BIG_ICON))
    this.buttonStop.setGraphic(ViewUtilities iconSetter(Material.STOP, JavafxEnums.BIG_ICON))
    this.buttonStep.setGraphic(ViewUtilities iconSetter(Material.SKIP_NEXT, JavafxEnums.BIG_ICON))
    this.buttonCreateParticles.setGraphic(ViewUtilities iconSetter(Material.BUBBLE_CHART, JavafxEnums.BIG_ICON))

    this.buttonStartPause setOnAction(_ => {
      this.buttonStop setDisable false
       this.buttonStartPause.getGraphic match {
        case this.startIcon =>  this.buttonStartPause setGraphic this.pauseIcon
          this.buttonPopup setDisable true
          this.buttonStep setDisable true
          this.startSimulation()
        case this.pauseIcon =>
          this.buttonPopup setDisable false
          this.buttonStep setDisable false
          this.buttonStartPause setGraphic this.startIcon
          this.pauseSimulation()
      }
    })
    this.buttonStop setDisable true
    this.buttonStop.setOnAction(_ => {
      this.buttonStartPause setDisable false
      this.buttonStop setDisable true
      this.buttonStep setDisable false
      this.buttonStartPause setGraphic this.startIcon
      this.stopSimulation()
    })
    this.buttonStep.setOnAction(_ => {
      this.buttonStop setDisable false
      this.stepSimulation()
    })
    this.buttonCreateParticles.setOnAction(_ => this.prepareSimulation())

    this.buttonPopup.setOnAction(_ => this.popup.show(this.mainBorder))
  }

  private def prepareScene3D(): Unit = {
    this.particles = new Group
    val scene3D = new SubScene(this.particles, 100, 100, true, SceneAntialiasing.BALANCED)
    this.stack3D.getChildren add scene3D
    this.camera = new PerspectiveCamera
    scene3D setCamera this.camera
    scene3D setRoot this.particles
    scene3D.widthProperty.bind(this.stack3D widthProperty)
    scene3D.heightProperty.bind(this.stack3D.heightProperty)
  }

  private def prepareHideToolbar(): Unit = {
    this.mainBorder.setOnMouseClicked(ev => {
      if (ev.getButton == MouseButton.SECONDARY && this.toolbar.isVisible) {
        this.toolbar setVisible false
      } else if (ev.getButton == MouseButton.SECONDARY && !this.toolbar.isVisible) {
        this.toolbar setVisible true
      }
    })
  }

  def startSimulation(): Unit
  def pauseSimulation(): Unit
  def stopSimulation(): Unit
  def stepSimulation(): Unit
  def prepareSimulation(): Unit
  def setParticles(amount: Int): Unit
  def setIteration(amount: Int): Unit
  def setTime(amount: Int, sliderMin: Double, sliderMax: Double): Unit

  protected final class PopupScreenView {
    @FXML protected var mainBorderPopup: BorderPane = _
    @FXML protected var sliderParticles: JFXSlider = _
    @FXML protected var sliderIteration: JFXSlider = _
    @FXML protected var sliderTimeStep: JFXSlider = _
    this.mainBorderPopup = ViewUtilities.loadFxml(this, POPUP_GUI).asInstanceOf[BorderPane]

    @FXML private[screens] def initialize(): Unit = {
      this.assertNodeInjected()
      this.prepareSliders()
    }

    def mainBorder(): BorderPane = mainBorderPopup

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