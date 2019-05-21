package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{ActorLogging, FSM}
import it.unibo.pcd1819.actorpositioning.actors.FiniteStateMachineController._
import it.unibo.pcd1819.actorpositioning.model.{Environment, Particle}

class FiniteStateMachineController extends FSM[State, Data] with ActorLogging{

  when(Idle) {
    case Event(Generate(n), _) => ???
    case Event(Add(x, y), _) => ???
    case Event(Remove(p), _) => ???
    case Event(Start, _) => ???
    case Event(Step, _) => ???

    case Event(Pause, _) => ???
    case Event(Result(e), _) => ???
    case Event(Stop, _) => ???
  }

  when(Waiting) {
    case Event(Add(x, y), _) => ???
    case Event(Remove(p), _) => ???
    case Event(Result(e), _) => ???
    case Event(Start, _) => ???
    case Event(Pause, _) => ???
    case Event(Stop, _) => ???

    case Event(Generate(n), _) => ???
    case Event(Step, _) => ???
  }

  when(Running) {
    case Event(Add(x, y), _) => ???
    case Event(Remove(p), _) => ???
    case Event(Step, _) => ???
    case Event(Pause, _) => ???
    case Event(Stop, _) => ???

    case Event(Generate(n), _) => ???
    case Event(Start, _) => ???
    case Event(Result(e), _) => ???
  }

  when(Paused) {
    case Event(Add(x, y), _) => ???
    case Event(Remove(p), _) => ???
    case Event(Start, _) => ???
    case Event(Step, _) => ???
    case Event(Stop, _) => ???

    case Event(Generate(n), _) => ???
    case Event(Pause, _) => ???
    case Event(Result(e), _) => ???
  }

//  onTransition {
//
//  }

//  whenUnhandled {
//
//  }

  initialize()
}

object FiniteStateMachineController {
  sealed trait State
  case object Idle extends State
  case object Running extends State
  case object Paused extends State
  case object Waiting extends State

  sealed trait Data
  final object NoData
  final case class Caller(s: State, createe: Seq[(Int, Int)], deletee: Seq[Particle]) extends Data

  sealed trait Input
  final case class Generate(n: Int) extends Input
  final case class Add(x: Int, y: Int) extends Input
  final case class Remove(p: Particle) extends Input
  final case class Result(e: Environment) extends Input
  case object Start extends Input
  case object Pause extends Input
  case object Stop extends Input
  case object Step extends Input

  //
  final case class Publish(env: Environment)
  //ToEnvironment
  //  final case class Generate(n: Int) extends Output
}
