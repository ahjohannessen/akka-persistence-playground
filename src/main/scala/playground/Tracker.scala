package playground

import akka.persistence.{Deliver, Channel, Persistent, Processor}
import akka.actor.{Props, ActorRef, Actor, ActorLogging}
import playground.Tracker.{TrackRegistered, TrackingEvent}

object Tracker {
  sealed trait TrackingCmd
  case class RegisterTrack(id: Int) extends TrackingCmd
  case object ReportNumberOfTracks extends TrackingCmd

  sealed trait TrackingEvent
  case class TrackRegistered(id: Int) extends TrackingEvent
}

class Tracker(listener: ActorRef) extends Processor with ActorLogging  {

  import Tracker._

  private val channel = context.actorOf(Props[Channel], name = "delivery-channel")
  private var state = TrackingState.empty

  def receive: Receive = {

    case Persistent(event: TrackingEvent, snr) if recoveryRunning ⇒ {
      log.info(s"Replaying $snr [${event.getClass.getSimpleName}]")
      applyEvent(event)
      deliverEvent(event)
    }

    case track: RegisterTrack ⇒ {
      handleDomainEvent(TrackRegistered(track.id)){ (e, snr) ⇒
        applyEvent(e)
        bomb(snr) // !!!
        deliverEvent(e)
      }
    }

    case ReportNumberOfTracks ⇒
      log.info(s"Number of tracks is ${state.numberOfTracks}")

  }

  def applyEvent(te: TrackingEvent) = {
    state = state.update(te)
  }

  def deliverEvent[T](event: T) = {
    currentPersistentMessage.foreach(p ⇒
      channel ! Deliver(p.withPayload(event), listener)
    )
  }

  def handleDomainEvent[A](event: A)(whenStored: (A, Long) ⇒ Unit) = {

    val storingBehavior: Receive = {
      case Persistent(`event`, snr) ⇒
        context.unbecome()
        unstashAll()
        whenStored(event, snr)
      case _ ⇒ stash()
    }
    context.become(storingBehavior, discardOld = false)
    self forward Persistent(event)
  }

  private def bomb(snr: Long) = snr match {
    case 2 ⇒ log.error("Simulating JVM crash") ; sys.exit(1)
    case 8 ⇒ log.error("Simulating RuntimeException") ; throw new RuntimeException
    case _ ⇒
  }

}

object TrackingState {
  def empty = apply(Set())
}

case class TrackingState private(tracks: Set[Int]) {

  def update: TrackingEvent ⇒ TrackingState = {
    case TrackRegistered(id) ⇒ copy(tracks = tracks + id)
  }
  def numberOfTracks = tracks.size
}

class Listener extends Actor with ActorLogging {
  def receive : Receive = {
    case p@Persistent(e, snr) ⇒ {
      log.info(s"Got $e with sequenceNr $snr")
      p.confirm()
    }
  }
}