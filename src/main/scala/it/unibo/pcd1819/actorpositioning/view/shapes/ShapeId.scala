package it.unibo.pcd1819.actorpositioning.view.shapes

import javafx.scene.paint.Paint
import javafx.scene.shape.{Circle, Rectangle, Shape}

trait ShapeId extends Shape {
  def id: Int
}

object Shapes {
  class RectangleId(x: Double,
                    y: Double,
                    width: Double,
                    height: Double,
                    override val id: Int) extends Rectangle(x, y, width, height) with ShapeId {}
  class CircleId(centerX: Double,
                 centerY: Double,
                 radius: Double,
                 fill: Paint,
                 override val id: Int) extends Circle(centerX, centerY, radius, fill) with ShapeId {}
}
