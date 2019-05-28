package it.unibo.pcd1819.actorpositioning.model

import scala.util.Random

object Constants {
    val universal: Double = 1.0
    val timeStep: Double = 1.0
}

case class Particle(position: Vector2D, mass: Double, charge: Double, id: Int)(velocity: Vector2D = Vector2D.zero, force: Vector2D = Vector2D.zero) {

    def applyForceFrom(that: Particle): Particle = {
        val distance = position distanceFrom that.position
        val distanceNorm = distance.norm3
        val appliedForce = distance * ((this.charge * that.charge * Constants.universal) / distanceNorm)
        this.copy()(velocity = this.velocity, force = appliedForce)
    }

    def commitForce(): Particle = {
        val acceleration = this.force * (1 / this.mass)
        val newPosition = this.position + this.velocity * Constants.timeStep
        val newVelocity = this.velocity + acceleration * Constants.timeStep
        val newForce = Vector2D.zero
        this.copy(position = newPosition)(velocity = newVelocity, force = newForce)
    }
}

object Particle {
    val massMultiplier = 100
    val chargeMultiplier = 1
    def apply(position: Vector2D, mass: Double, charge: Double, id: Int): Particle =
        new Particle(position, mass, charge, id: Int)(Vector2D.zero, Vector2D.zero)
    def random(range: Double, id: Int): Particle = {
        val randomX = 2 * range * Random.nextDouble() - range
        val randomY = 2 * range * Random.nextDouble() - range
        randomAt(randomX, randomY, id)
    }
    def randomAt(x: Double, y: Double, id: Int): Particle = {
        val position = Vector2D(x, y)
        val randomMass = Random.nextDouble() * massMultiplier
        val randomCharge = Random.nextDouble() * chargeMultiplier
        Particle(position, randomMass, randomCharge, id)
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
    def random(range: Double): Vector2D = {
        val randomX = 2 * range * Random.nextDouble() - range
        val randomY = 2 * range * Random.nextDouble() - range
        Vector2D(randomX, randomY)
    }
}