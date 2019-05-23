package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{ActorLogging, FSM, Props}
import it.unibo.pcd1819.actorpositioning.actors.ControllerFSM._
import it.unibo.pcd1819.actorpositioning.model.{Environment, Particle}

object ControllerFSM {
  sealed trait State
  final object Pit extends State
  final object Idle extends State
  final object Running extends State
  final object Paused extends State

  sealed trait Data
  final case object NoData extends Data
  final case class Request(i: Input) extends Data

  sealed trait Input
  final case object Start extends Input
  final case object Pause extends Input
  final case object Stop extends Input
  final case object Step extends Input
  final case class Generate(n: Int) extends Input
  final case class Add(x: Double, y: Double) extends Input
  final case class Remove(p: Particle) extends Input
//  final case class Result(e: Seq[(Int, Int)]) extends Input
  final case class Result(e: Environment) extends Input

  def props = Props(classOf[ControllerFSM])
}

object Constants {
  val radius : Double = 100.0
}

class ControllerFSM extends FSM[State, Data] with ActorLogging{

  private val environment = context actorOf(EnvironmentActor props, "environment")
  private val view = context actorOf(ViewActor props, "view")

  startWith(Idle, NoData)

  when(Idle) {
    case Event(Start, _) => goto(Running).using(NoData);
    case Event(Step, _) => goto(Paused).using(NoData)
    case Event(Pause, _) => goto(Pit)
    case Event(Stop, _) => goto(Pit)
    case Event(Generate(n), _) => goto(Idle).using(Request(Generate(n)))
    case Event(Add(x, y), _) => goto(Idle).using(Request(Add(x, y)))
    case Event(Remove(p), _) => goto(Idle).using(Request(Remove(p)))
    case Event(Result(e), _) => goto(Idle).using(Request(Result(e)))
  }

  when(Running) {
    case Event(Start, _) => goto(Pit)
    case Event(Step, _) => goto(Running).using(Request(Step))
    case Event(Pause, _) => goto(Paused).using(NoData)
    case Event(Stop, _) => goto(Idle).using(NoData)
    case Event(Generate(_), _) => goto(Pit)
    case Event(Add(x, y), _) => goto(Running).using(Request(Add(x, y)))
    case Event(Remove(p), _) => goto(Running).using(Request(Remove(p)))
    case Event(Result(e), _) => goto(Running).using(Request(Result(e)))
  }

  when(Paused) {
    case Event(Start, _) => goto(Running).using(NoData)
    case Event(Step, _) => goto(Paused).using(Request(Step))
    case Event(Pause, _) => goto(Pit)
    case Event(Stop, _) => goto(Idle).using(NoData)
    case Event(Generate(_), _) => goto(Pit)
    case Event(Add(x, y), _) => goto(Paused).using(Request(Add(x,y)))
    case Event(Remove(p), _) => goto(Paused).using(Request(Remove(p)))
    case Event(Result(e), _) => goto(Paused).using(Request(Result(e)))
  }

  when(Pit) {
    case _ => stay()
  }

  onTransition {
    case _ -> Pit => log debug "Pit"

    case Idle -> Idle =>
      nextStateData match {
        case Request(Generate(n)) =>
          log debug s"Idle asked to generate $n particles"
          environment ! EnvironmentActor.Generate(n, Constants.radius)
        case Request(Add(x, y)) =>
          log debug s"Idle asked for creation of $x, $y"
          environment ! EnvironmentActor.Add(x, y)
        case Request(Remove(p)) =>
          log debug s"Idle asked for removal of $p"
          environment ! EnvironmentActor.Remove(p)
        case Request(Result(e)) =>
          log debug s"Idle asked to publish $e"
          view ! ViewActor.Publish(e)
        case NoData => log debug "Initializing FSM"
        case _ => log debug "Report this message ASAP"
      }
    case Idle -> Running =>
      log debug "Idle to Running"
      environment ! EnvironmentActor.Start
      environment ! EnvironmentActor.Step
    case Idle -> Paused =>
      log debug "Idle to Paused"

    case Running -> Running =>
      nextStateData match {
        case Request(Add(x, y)) =>
          log debug s"Running asked for creation of $x, $y"
          environment ! EnvironmentActor.Add(x, y)
        case Request(Remove(p)) =>
          log debug s"Running asked for removal of $p"
          environment ! EnvironmentActor.Remove(p)
        case Request(Result(e)) =>
          log debug s"Running asked to publish $e and to perform an additional Step"
          view ! ViewActor.Publish(e)
          environment ! EnvironmentActor.Step
        case _ => log debug "Report this message ASAP"
      }
    case Running -> Idle =>
      log debug "Running to Idle"
      environment ! EnvironmentActor.Stop
    case Running -> Paused =>
      log debug "Running to Paused"

    case Paused -> Paused =>
      nextStateData match {
        case Request(Add(x, y)) =>
          log debug s"Paused asked for creation of $x, $y"
          environment ! EnvironmentActor.Add(x, y)
        case Request(Remove(p)) =>
          log debug s"Paused asked for removal of $p"
          environment ! EnvironmentActor.Remove(p)
        case Request(Result(e)) =>
          log debug s"Paused asked to publish $e"
          view ! ViewActor.Publish(e)
        case Request(Step) =>
          log debug s"Paused asked to perform another Step"
          environment ! EnvironmentActor.Step
        case _ => log debug "Report this message ASAP"
      }
    case Paused -> Idle =>
      log debug "Paused to Idle"
      environment ! EnvironmentActor.Stop
    case Paused -> Running =>
      log debug "Paused to Running"
      environment ! EnvironmentActor.Step
  }
  initialize()
}