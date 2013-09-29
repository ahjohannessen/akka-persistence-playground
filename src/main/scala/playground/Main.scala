package playground

import akka.actor.{Props, ActorSystem}
import akka.persistence.Persistent
import playground.MasterOfPuppets._
import playground.Puppet._

object Main {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("playground")

    val masterOfPuppets = system.actorOf(
      Props[MasterOfPuppets],
      name = "master-of-puppets"
    )

    (1 to 10) foreach { id =>
      masterOfPuppets ! Persistent(IntroducePuppet(id))
      masterOfPuppets ! Greeting(id, "Welcome")
    }

    Thread.sleep(1000)

    (5 to 10) foreach { id =>
       masterOfPuppets ! Persistent(ArchivePuppet(id))
    }

  }
}