package it.unibo.pcd1819.actorpositioning.view

object FXMLScreens {
  sealed abstract class FXMLScreens(val resourcePath: String, val cssPath: String) {}
  case object HOME extends FXMLScreens("/screens/MainScreen.fxml", "/sheets/MainScreen.css")
  case object POPUP_GUI extends FXMLScreens("/screens/PopupScreen.fxml", "/sheets/MainScreen.css")
}