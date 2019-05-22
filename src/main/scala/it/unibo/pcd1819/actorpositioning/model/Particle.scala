package it.unibo.pcd1819.actorpositioning.model

import scala.util.Random

object Constants {
    val universal: Double = 1.0
    val timeStep: Double = 1.0
}

case class Particle(var position: Vector2D, mass: Double, charge: Double)(val id: Int) {
    var velocity: Vector2D = Vector2D()
    var force: Vector2D = Vector2D()

    def applyForceFrom(that: Particle): Unit = {
        val distance = position distanceFrom that.position
        val distanceNorm = distance.norm3
        val appliedForce = distance * ((this.charge * that.charge * Constants.universal) / distanceNorm)
        this.force = this.force + appliedForce
    }

    def commitForce(): Unit = {
        val acceleration = this.force * (1 / this.mass)
        this.position = this.position + this.velocity * Constants.timeStep
        this.velocity = this.velocity + acceleration * Constants.timeStep
        this.force = Vector2D.zero
    }
}

object Particle {
    def random(within: Double, id: Int): Particle = {
        val randomPosition = Vector2D.random(within)
        val randomMass = Random.nextDouble()
        val randomCharge = Random.nextDouble()
        Particle(randomPosition, randomMass, randomCharge)(id)
    }
}

case class Vector2D(x: Double, y: Double) {
    def +(that: Vector2D): Vector2D = Vector2D(x + that.x, y + that.y)
    def *(scalar: Double) = Vector2D(x * scalar, y * scalar)
    def norm: Double = Math.sqrt(x * x + y * y)
    def norm3: Double = Math.pow(this.norm, 3)
    def distanceFrom(that: Vector2D): Vector2D = {
        val distanceX: Double = Math.abs(x - that.x)
        val distanceY: Double = Math.abs(y - that.y)
        Vector2D(distanceX, distanceY)
    }
}

object Vector2D {
    def apply(): Vector2D = new Vector2D(0, 0)
    def zero = Vector2D(0, 0)
    def random(within: Double): Vector2D = {
        val randomX = within + 2 * within * Random.nextDouble()
        val randomY = within + 2 * within * Random.nextDouble()
        Vector2D(randomX, randomY)
    }
}