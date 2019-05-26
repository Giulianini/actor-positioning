package it.unibo.pcd1819.actorpositioning.view.utilities

import it.unibo.pcd1819.actorpositioning.model.{Particle, Vector2D}
import it.unibo.pcd1819.actorpositioning.view.utilities.JavafxEnums.ShapeType._
import javafx.scene.paint.Color
import javafx.scene.shape.{Circle, Rectangle, Shape}

object ParticleDrawingUtils {
  private val HUE = 0
  private val MAX_COLOR_VALUE = 100
  private val BRIGHTNESS = 0.90

  def createParticleShapes(particle: Particle, shapeType: Value, environmentSize: Vector2D): Shape = {
    val particleRadius = particle.mass
    val chargeColor = particle.charge / ParticleDrawingUtils.MAX_COLOR_VALUE
    val color = Color.hsb(ParticleDrawingUtils.HUE, chargeColor, ParticleDrawingUtils.BRIGHTNESS)
    val posX: Double = particle.position.x + environmentSize.x * 0.5
    val posY: Double = particle.position.y + environmentSize.y * 0.5
    val shape: Shape = shapeType match {
      case RECTANGULAR => val rec = new Rectangle(posX - particleRadius / 2, posY - particleRadius / 2, particleRadius, particleRadius); rec.setFill(color); rec
      case CIRCLE => new Circle(posX, posY, particleRadius, color)
    }
    shape.setSmooth(true)
    shape
  }
}
