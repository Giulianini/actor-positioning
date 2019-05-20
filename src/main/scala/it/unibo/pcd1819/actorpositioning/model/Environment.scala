package it.unibo.pcd1819.actorpositioning.model

import scala.collection.mutable

class Environment {

    private val env : mutable.Set[Particle] = mutable.Set()

    def environment : Seq[Particle] = env toList

    def add(particle: Particle): Unit = env += particle

    def remove(particle: Particle) : Unit = env -= particle

}
