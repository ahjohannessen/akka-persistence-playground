package playground

import akka.actor._
import akka.persistence._
import scala.concurrent.duration._
import playground.MasterOfPuppets._
import playground.Puppet._

object MasterOfPuppets {
  case class IntroducePuppet(id: Int)
  case class ArchivePuppet(id: Int)

  private case object StartActivePuppets
}

class MasterOfPuppets extends Processor with ActorLogging {

  var activePuppets = Set[Int]()

  override def preStart() = {
    super.preStart()
    context.system.scheduler.scheduleOnce(1.second, self, StartActivePuppets)(context.dispatcher)
  }

  def receive = {
    case Persistent(IntroducePuppet(id), snr) => introducePuppet(id)
    case Persistent(ArchivePuppet(id), snr)   => archivePuppet(id)
    case StartActivePuppets                   => startActivePuppets()
    case pm: PuppetMessage                    => forwardToPuppet(pm)
  }

  def forwardToPuppet(pm: PuppetMessage) = {
    puppetRef(pm.id) forward Persistent(pm)
  }

  def archivePuppet(puppetId: Int) = {
    activePuppets = activePuppets - puppetId
    context.child(puppetId.toString).foreach(_ ! PoisonPill)
  }

  def introducePuppet(puppetId: Int) = {
    activePuppets = activePuppets + puppetId
    if(!recoveryRunning) puppetRef(puppetId)
  }

  def puppetRef(puppetId: Int): ActorRef = {
    val puppetName = puppetId.toString
    context.child(puppetName) match {
      case None      => context.actorOf(Props[Puppet], puppetName)
      case Some(ref) => ref
    }
  }

  def startActivePuppets() =
    activePuppets.foreach(puppetRef)

}

object Puppet {
  trait PuppetMessage {val id: Int }
  case class Greeting(id: Int, msg: String) extends PuppetMessage
}

class Puppet extends Processor with ActorLogging {

  def receive = {
    case Persistent(greet: Greeting, snr) =>
      log.info(s"Got greeting: ${greet.msg}")
  }

  override def postStop() = {
    log.info("So long and thanks for all the fish...")
    super.postStop()
  }
}