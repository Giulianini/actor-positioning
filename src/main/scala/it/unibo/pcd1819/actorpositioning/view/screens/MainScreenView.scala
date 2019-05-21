package it.unibo.pcd1819.actorpositioning.view.screens

import javafx.application.Platform.runLater
import javafx.fxml.FXML
import javafx.scene.layout.AnchorPane

class MainScreenView {
  @FXML
  private var mainBorder = null
  @FXML
  private val labelExecutionTime = null
  @FXML
  private val sliderWorkers = null
  @FXML
  private val comboBoxOptimize = null
  @FXML
  private val comboBoxShape = null
  MainScreenView() {
    runLater(() => this.mainBorder = ViewUtilities.loadFxml(this, FXMLScreens.HOME).asInstanceOf[AnchorPane])
  }
}

object MainScreenView {
  def apply: MainScreenView = new MainScreenView()
}
