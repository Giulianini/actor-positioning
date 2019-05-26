package it.unibo.pcd1819.actorpositioning.view.utilities

import it.unibo.pcd1819.actorpositioning.model.{Particle, Vector2D}
import it.unibo.pcd1819.actorpositioning.view.utilities.JavafxEnums.{CIRCLE, RECTANGULAR, ShapeType}
import javafx.scene.paint.Color
import javafx.scene.shape.{Circle, Rectangle, Shape}

object ParticleDrawingUtils {
  private val HUE = 0
  private val MAX_COLOR_VALUE = 100
  private val BRIGHTNESS = 0.90

  def createParticleShapes(particle: Particle, shapeType: ShapeType, environmentSize: Vector2D): Shape = {
    val particleRadius = particle.mass
    val chargeColor = particle.charge / ParticleDrawingUtils.MAX_COLOR_VALUE
    val color = Color.hsb(ParticleDrawingUtils.HUE, chargeColor, ParticleDrawingUtils.BRIGHTNESS)
    val shape: Shape = shapeType match {
      case RECTANGULAR => new Rectangle(particleRadius, particleRadius, color)
      case CIRCLE => new Circle(particleRadius, color)
    }
    shape.setSmooth(true)
    shape.setTranslateX(particle.position.x + environmentSize.x * 0.5 - particleRadius / 2)
    shape.setTranslateY(particle.position.y + environmentSize.y * 0.5 - particleRadius / 2)
    shape
  }
}
