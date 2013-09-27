package playground

import akka.actor.{Props, ActorSystem}
import playground.Tracker._

object Main {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("playground")
    val listenerA = system.actorOf(Props[Listener], name = "listener-a")
    val listenerB = system.actorOf(Props[Listener], name = "listener-b")
    val tracker = system.actorOf(Props(classOf[Tracker], listenerA), "tracker")

    (1 to 10) foreach {
      tracker ! Tracker.RegisterTrack(_)
    }

    Thread.sleep(1000)
    tracker ! ReportNumberOfTracks

  }
}