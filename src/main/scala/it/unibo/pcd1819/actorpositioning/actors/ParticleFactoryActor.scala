package it.unibo.pcd1819.actorpositioning.actors

import it.unibo.pcd1819.actorpositioning.actors.ParticleFactoryActor.{GenerateRandomParticle, NewParticles}
import akka.actor.{Actor, Props}
import it.unibo.pcd1819.actorpositioning.model.Particle

class ParticleFactoryActor extends Actor {

    private var idCounter = 0

    override def receive: Receive = {
        case GenerateRandomParticle(amount, range) =>
            sender() ! NewParticles(0 until amount map (_ => {
                val particle = Particle random (range, idCounter)
                idCounter += 1
                particle
            }))
    }
}

object ParticleFactoryActor {
    final case class GenerateRandomParticle(amount: Int, range: Double)
    final case class NewParticles(particles: Seq[Particle])

    val path = "particle-factory"

    def props = Props(classOf[ParticleFactoryActor])
}
