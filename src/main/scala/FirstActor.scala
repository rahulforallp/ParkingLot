import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._


object Factory {
  val list=ArrayBuffer.fill(10)(false)
  val system = ActorSystem("PL")
  val slotMonitor = system.actorOf(Props[SlotMonitor], "SlotMonitor")
  val attendant = system.actorOf(Props[Attendant], "Attendant")
}

class SlotMonitor extends Actor {
  def receive ={
    case "BookSlot" => {
      val slot = Factory.list.indexOf(false)
      if (slot >= 0) {
        Factory.list.update(slot, true)
      }
      sender ! slot
    }
    case slot:Int => {
      if (slot >= 0 && slot <=10) {
        Factory.list.update(slot, false)
      }
      sender ! slot
    }
  }
}

class Attendant extends Actor {
  implicit val timeout = Timeout(5.seconds)
  def receive ={
    case "ParkMe" => {
      val parkingSlot =Await.result((Factory.slotMonitor ? "BookSlot").mapTo[Int], 5.seconds)
      println("Park at "+parkingSlot)
    }
    case departMe:Int => val parkingSlot =Await.result((Factory.slotMonitor ? departMe).mapTo[Int], 5.seconds)
      println("Departed at "+parkingSlot)
  }
}

object StartParking extends App{

  Factory.attendant ! "ParkMe"
  Factory.attendant ! "ParkMe"
  Factory.attendant ! "ParkMe"
  Factory.attendant ! "ParkMe"
  Factory.attendant ! 2
  Factory.attendant ! "ParkMe"

}

