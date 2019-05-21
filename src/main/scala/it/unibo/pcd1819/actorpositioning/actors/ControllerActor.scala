package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{Actor, ActorLogging, ActorRef, FSM, Props}
import it.unibo.pcd1819.actorpositioning.actors.ControllerActor._
import it.unibo.pcd1819.actorpositioning.model.{Environment, Particle}

class ControllerActor extends FSM[State, Data] with ActorLogging{
  import ControllerActor._

//  private val environment = context actorOf(EnvironmentActor props)
//  private val view = context actorOf(ViewActor props)
//
//  startWith(Uninitialized, NoData)
//
//  when(Uninitialized) {
//    case Event(Generate(n), gen: Generation) =>
//      goto(Idle).using(gen(n))
//    case Event(Add(x, y), add: Addition) =>
//      goto(Idle).using(add(x, y))
//    case _ => ???
//  }
//
//  when(Idle) {
//    case Event(Remove(p), remove: Removal) =>
//      stay.using(remove(p))
//    case Event(Add(x, y), add: Addition) =>
//      stay.using(add(x, y))
//    case Event(Generate(n), gen: Generation) =>
//      stay.using(gen(n))
//    case Event(Start, NoData) =>
//      goto(Running).using(NoData)
//    case Event(Step, NoData) =>
//      goto(Paused).using(NoData)
//    case Event(Empty, NoData) =>
//      goto(Uninitialized).using(NoData)
//
//    case _ => ???
//  }
//
//  when(Running) {
//    case Event(Remove(p), remove: Removal) =>
//      stay.using(remove(p))
//    case Event(Add(x, y), add: Addition) =>
//      stay.using(add(x, y))
//    case Event(Step, NoData) =>
//      goto(Waiting).using(NoData)
//    case Event(Pause, NoData) =>
//      goto(Paused).using(NoData)
//    case Event(Stop, NoData) =>
//      goto(Idle).using(NoData)
//
//    case _ => ???
//  }
//
//  when(Paused) {
//    case Event(Remove(p), remove: Removal) =>
//      stay.using(remove(p))
//    case Event(Add(x, y), add: Addition) =>
//      stay.using(add(x, y))
//    case Event(Step, NoData) =>
//      goto(Waiting).using(NoData)
//    case Event(Start, NoData) =>
//      goto(Running).using(NoData)
//    case Event(Stop, NoData) =>
//      goto(Idle).using(NoData)
//
//    case _ => ???
//  }
//
//  when(Waiting) {
//    //very problem -> add and remove need to be able to detect the end of the step before asking for modification
//
//    case Event(SyncResult(env), res: Result) =>
//      //goto
//
//    case _ => ???
//  }
//
//  onTransition {
//    case Uninitialized -> Idle =>
//      stateData match {
//        case Generation(n) => environment ! EnvironmentActor.Generate(n)
//        case Addition(x, y) => environment ! EnvironmentActor.Add(x, y)
//        case _ => ???
//      }
//    case _ => ???
//  }
//
//  initialize()
}

object ControllerActor {
  def props = Props(new ControllerActor())

  sealed trait Receiving
  final case class Generate(numberOfParticles: Int) extends Receiving
  final case class Add(x: Int, y: Int) extends Receiving
  final case class Remove(particle: Particle) extends Receiving
  final case class SyncResult(environment: Environment) extends Receiving
  case object Empty extends Receiving
  case object Start extends Receiving
  case object Pause extends Receiving
  case object Resume extends Receiving
  case object Stop extends Receiving
  case object Step extends Receiving

  sealed trait State
  case object Uninitialized extends State
  case object Idle extends State
  case object Running extends State
  case object Paused extends State
  case object Waiting extends State

  sealed trait Data
  case object NoData extends Data
  final case class Generation(numberOfParticles: Int) extends Data
  final case class Addition(x: Int, y: Int) extends Data
  final case class Removal(particle: Particle) extends Data
  final case class Result(environment: Environment) extends Data
}

object Constants {
  val radius : Double = 100.0
}
