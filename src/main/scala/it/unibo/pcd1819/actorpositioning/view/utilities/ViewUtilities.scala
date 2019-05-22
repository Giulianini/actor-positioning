package it.unibo.pcd1819.actorpositioning.view.utilities

import it.unibo.pcd1819.actorpositioning.view.{FXMLScreens, HOME}
import it.unibo.pcd1819.actorpositioning.view.screens.{MainScreenView, View}
import javafx.fxml.FXMLLoader
import javafx.scene.layout.AnchorPane
import javafx.scene.{Node, Scene}

object ViewUtilities {
  def loadFxml(controller: Any, fxml: FXMLScreens): Node = {
    val loader = new FXMLLoader( classOf[MainScreenView].getResource(fxml.resourcePath))
    loader.setController(controller)
    val root: AnchorPane = loader.load()
    root
  }

  def chargeSceneSheets(scene: Scene): Unit = {
    scene.getStylesheets.add(classOf[MainScreenView].getResource(HOME.cssPath).toString)
  }
}
