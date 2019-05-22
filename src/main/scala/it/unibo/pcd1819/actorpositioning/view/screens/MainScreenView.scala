package it.unibo.pcd1819.actorpositioning.view.screens

import com.jfoenix.controls.{JFXComboBox, JFXSlider}
import com.sun.javafx.application.PlatformImpl
import it.unibo.pcd1819.actorpositioning.view.HOME
import it.unibo.pcd1819.actorpositioning.view.utilities.ViewUtilities
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.Scene
import javafx.stage.Stage
import scalafx.scene.layout.AnchorPane

trait View {
}

case class MainScreenView() extends View {
  @FXML
  var mainBorder: AnchorPane = _
  mainBorder = ViewUtilities.loadFxml(this, HOME).asInstanceOf[AnchorPane]

  @FXML
  def initialize: Unit = {
    val stage = new Stage
    val scene = new Scene(this.mainBorder)
    stage.setScene(scene)
    ViewUtilities.chargeSceneSheets(scene)
    stage.show()
  }
}

object MainScreenView {
  def apply: MainScreenView = new MainScreenView()
}

object Main extends App {
  PlatformImpl.startup(() => {})
  Platform.runLater(() => new MainScreenView())
}
