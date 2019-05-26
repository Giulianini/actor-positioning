package it.unibo.pcd1819.actorpositioning.actors

import it.unibo.pcd1819.actorpositioning.actors.ParticleFactoryActor.{GenerateRandomParticles, NewParticles}
import akka.actor.{Actor, ActorLogging, Props}
import it.unibo.pcd1819.actorpositioning.model.Particle

class ParticleFactoryActor extends Actor with ActorLogging {

    private var idCounter = 0

    override def receive: Receive = {
        case GenerateRandomParticles(amount, range) =>
            log debug s"Generating $amount particles within $range"
            sender() ! NewParticles(0 until amount map (_ => {
                val particle = Particle random (range, idCounter)
                idCounter += 1
                particle
            }))
    }
}

object ParticleFactoryActor {
    final case class GenerateRandomParticles(amount: Int, range: Double)
    final case class NewParticles(particles: Seq[Particle])

    val name = "particle-factory"

    def props = Props(classOf[ParticleFactoryActor])
}
