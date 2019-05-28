package it.unibo.pcd1819.actorpositioning.view.utilities

import it.unibo.pcd1819.actorpositioning.model.{Particle, Vector2D}
import it.unibo.pcd1819.actorpositioning.view.shapes.ShapeId
import it.unibo.pcd1819.actorpositioning.view.shapes.Shapes.{CircleId, RectangleId}
import it.unibo.pcd1819.actorpositioning.view.utilities.JavafxEnums.ShapeType._
import javafx.scene.paint.Color

object ParticleDrawingUtils {
  private val HUE = 0
  private val MAX_COLOR_VALUE = 100
  private val BRIGHTNESS = 0.90

  def createParticleShapes(particle: Particle, shapeType: Value, environmentSize: Vector2D, id: Int): ShapeId = {
    val particleRadius = particle.mass
    val chargeColor = particle.charge / ParticleDrawingUtils.MAX_COLOR_VALUE
    val color = Color.hsb(ParticleDrawingUtils.HUE, chargeColor, ParticleDrawingUtils.BRIGHTNESS)
    val posX: Double = particle.position.x + environmentSize.x * 0.5
    val posY: Double = particle.position.y + environmentSize.y * 0.5
    val shape: ShapeId = shapeType match {
      case RECTANGULAR => val rec = new RectangleId(posX - particleRadius / 2, posY - particleRadius / 2, particleRadius, particleRadius, id); rec.setFill(color); rec
      case CIRCLE => new CircleId(posX, posY, particleRadius, color, id)
    }
    shape.setSmooth(true)
    shape
  }
}
