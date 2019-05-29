package it.unibo.pcd1819.actorpositioning.actors

import akka.actor.{ActorLogging, FSM, Props}
import it.unibo.pcd1819.actorpositioning.actors.ControllerFSM._
import it.unibo.pcd1819.actorpositioning.model.Particle

object ControllerFSM {
  sealed trait State
  final case object Pit extends State
  final case object Idle extends State
  final case object Running extends State
  final case object Paused extends State

  sealed trait Data
  final case class Request(s: Settings, i: Input) extends Data

  sealed trait Input
  final case object NoInput extends Input
  final case object Start extends Input
  final case object Pause extends Input
  final case object Stop extends Input
  final case object Step extends Input
  final case object GenerateEnvironment extends Input
  final case class UpdateParticles(n: Int) extends Input
  final case class UpdateIterations(n: Int) extends Input
  final case class UpdateTimeStep(n: Int) extends Input
  final case class Add(x: Double, y: Double) extends Input
  final case class Remove(id: Int) extends Input
  final case class Result(e: Seq[Particle]) extends Input

  final case class Settings(particles: Int, iterations: Int, timeStep: Int)
  def props = Props(new ControllerFSM())
}

class ControllerFSM extends FSM[State, Data] with ActorLogging {

  private val environment = context actorOf(EnvironmentActor.props, "environment")

  import DefaultConstants._
  import EnvironmentConstants._

  private val view = context actorOf(ViewActor.props(DEFAULT_PARTICLES, DEFAULT_ITERATIONS, DEFAULT_TIME_STEP, RADIUS), "view")
  private val settings = Settings(DEFAULT_PARTICLES, DEFAULT_ITERATIONS, DEFAULT_TIME_STEP)
  private var startingTime: Long = _
  private var stopwatch: Long = _
  private var actualTime: Long = 0

  startWith(Idle, Request(settings, NoInput))

  when(Idle) {
    case Event(Start, _) => goto(Running)
    case Event(Step, _) => goto(Paused)
    case Event(Pause, _) => goto(Pit)
    case Event(Stop, _) => goto(Pit)
    case Event(GenerateEnvironment, Request(s, _)) => goto(Idle).using(Request(s, GenerateEnvironment))
    case Event(UpdateParticles(n), Request(s, _)) => goto(Idle).using(Request(s.copy(particles = n), UpdateParticles(n)))
    case Event(UpdateIterations(n), Request(s, _)) => goto(Idle).using(Request(s.copy(iterations = n), UpdateIterations(n)))
    case Event(UpdateTimeStep(n), Request(s, _)) => goto(Idle).using(Request(s.copy(timeStep = n), UpdateTimeStep(n)))
    case Event(Add(x, y), Request(s, _)) => goto(Idle).using(Request(s, Add(x, y)))
    case Event(Remove(p), Request(s, _)) => goto(Idle).using(Request(s, Remove(p)))
    case Event(Result(e), Request(s, _)) => goto(Idle).using(Request(s, Result(e)))
  }

  when(Running) {
    case Event(Start, _) => goto(Pit)
    case Event(Step, Request(Settings(p, i, t), _)) => goto(Running).using(Request(Settings(p, i - 1, t), Step))
    case Event(Pause, _) => goto(Paused)
    case Event(Stop, _) => goto(Idle)
    case Event(GenerateEnvironment, _) => goto(Pit)
    case Event(UpdateIterations(_), _) => goto(Pit)
    case Event(UpdateTimeStep(_), _) => goto(Pit)
    case Event(Add(x, y), Request(s, _)) => goto(Running).using(Request(s, Add(x, y)))
    case Event(Remove(p), Request(s, _)) => goto(Running).using(Request(s, Remove(p)))
    case Event(Result(e), Request(s, _)) => goto(Running).using(Request(s, Result(e)))
  }

  when(Paused) {
    case Event(Start, _) => goto(Running)
    case Event(Step, Request(Settings(p, i, t), _)) => goto(Paused).using(Request(Settings(p, i - 1, t), Step))
    case Event(Pause, _) => goto(Pit)
    case Event(Stop, _) => goto(Idle)
    case Event(GenerateEnvironment, _) => goto(Pit)
    case Event(UpdateIterations(_), _) => goto(Pit)
    case Event(UpdateTimeStep(_), _) => goto(Pit)
    case Event(Add(x, y), Request(s, _)) => goto(Paused).using(Request(s, Add(x, y)))
    case Event(Remove(p), Request(s, _)) => goto(Paused).using(Request(s, Remove(p)))
    case Event(Result(e), Request(s, _)) => goto(Paused).using(Request(s, Result(e)))
  }

  when(Pit) {
    case _ => goto(Pit)
  }

  onTransition {
    case Pit -> Pit => log error "You are in the Pit"
    case _ -> Pit => log error "This action brings to the Pit"

    case Idle -> Idle =>
      nextStateData match {
        case Request(Settings(p, _, _), GenerateEnvironment) =>
          log debug s"Idle asked to generate environment"
          environment ! EnvironmentActor.Generate(p, EnvironmentConstants.RADIUS)
        case Request(_, Add(x, y)) =>
          log debug s"Idle asked for creation of $x, $y"
          environment ! EnvironmentActor.Add(x, y)
        case Request(_, Remove(p)) =>
          log debug s"Idle asked for removal of $p"
          environment ! EnvironmentActor.Remove(p)
        case Request(_, Result(e)) =>
          log info s"Idle asked to publish $e"
          view ! ViewActor.Publish(e, actualTime)
        case Request(_, UpdateTimeStep(n)) =>
          log debug s"Idle updated timeStep with $n"
          environment ! EnvironmentActor.SetTimeStep(n)
        case Request(_, UpdateIterations(n)) =>
          log debug s"Idle updated iterations with $n"
        case Request(_, NoInput) =>
          log info "Initializing FSM"
        case _ => log error "Report this message ASAP"
      }
    case Idle -> Running =>
      log info "Idle to Running"
      startingTime = System.currentTimeMillis()
      environment ! EnvironmentActor.Start
      environment ! EnvironmentActor.Step
    case Idle -> Paused =>
      startingTime = System.currentTimeMillis()
      log info "Idle to Paused"

    case Running -> Running =>
      nextStateData match {
        case Request(_, Add(x, y)) =>
          log info s"Running asked for creation of $x, $y"
          environment ! EnvironmentActor.Add(x, y)
        case Request(_, Remove(p)) =>
          log info s"Running asked for removal of $p"
          environment ! EnvironmentActor.Remove(p)
        case Request(Settings(_, 0, _), Result(e)) =>
          log debug s"Running sent the last environment to be published and is going to shutdown"
          view ! ViewActor.Publish(e, actualTime)
          self ! ControllerFSM.Stop
        case Request(Settings(_, i, _), Result(e)) =>
          log info s"Running asked to publish $e and to perform an additional Step"
          view ! ViewActor.Publish(e, actualTime)
          self ! ControllerFSM.Step
        case Request(Settings(_, i, _), Step) =>
          log info s"Remaining iterations: $i"
          environment ! EnvironmentActor.Step
        case _ => log error "Report this message ASAP"
      }
    case Running -> Idle =>
      log info "Running to Idle"
      environment ! EnvironmentActor.Stop
    case Running -> Paused =>
      log info "Running to Paused"

    case Paused -> Paused =>
      nextStateData match {
        case Request(_, Add(x, y)) =>
          log info s"Paused asked for creation of $x, $y"
          environment ! EnvironmentActor.Add(x, y)
        case Request(_, Remove(p)) =>
          log info s"Paused asked for removal of $p"
          environment ! EnvironmentActor.Remove(p)
        case Request(_, Result(e)) =>
          log info s"Paused asked to publish $e"
          view ! ViewActor.Publish(e, actualTime)
        case Request(Settings(_, 0, _), Step) =>
          log debug "Paused can no longer perform any Step and is going to shutdown"
          self ! ControllerFSM.Stop
        case Request(Settings(_, i, _), Step) =>
          log info s"Paused asked to perform another Step, remaining iterations: $i"
          environment ! EnvironmentActor.Step
        case _ => log error "Report this message ASAP"
      }
    case Paused -> Idle =>
      log info "Paused to Idle"
      environment ! EnvironmentActor.Stop
    case Paused -> Running =>
      log info "Paused to Running"
      environment ! EnvironmentActor.Step
  }
  initialize()
}

private object DefaultConstants {
  val DEFAULT_PARTICLES: Int = 50
  val DEFAULT_ITERATIONS: Int = 20000
  val DEFAULT_TIME_STEP: Int = 40
}

object EnvironmentConstants {
  val RADIUS: Double = 100.0
}