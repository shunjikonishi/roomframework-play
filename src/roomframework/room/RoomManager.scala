package roomframework.room

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.Props
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.Logger
import play.api.libs.iteratee.Iteratee
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Input
import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

class RoomManager(factory: RoomFactory) {
  
  implicit val timeout = Timeout(5)
  private var rooms = Map.empty[String, Room]
  private val actor = Akka.system.actorOf(Props(new MyActor()))
  
  def join(room: String): RoomHandler = {
    val ret = (actor ? Join(room)).asInstanceOf[Future[RoomHandler]]
    Await.result(ret, Duration.Inf)
  }
  
  protected def terminate() = {
    rooms.values.filter(_.isActive).foreach(_.close)
    rooms = Map.empty[String, Room]
  }
  
  private def getRoom(name: String): Room = {
    val room = rooms.get(name).filter(_.isActive)
    room match {
      case Some(x) => x
      case None =>
        val ret = factory.createRoom(name)
          rooms = rooms + (name -> ret)
          ret
    }
  }

  private sealed class Msg
  private case class Join(name: String)
  
  private class MyActor extends Actor {
    def receive = {
      case Join(name) =>
        val room = getRoom(name)
        val cnt = room.join
        sender ! factory.createHandler(room)
    }
    
    override def postStop() = {
      terminate()
      super.postStop()
    }
  }
  
} 

object RoomManager {
  def apply(factory: RoomFactory) = new RoomManager(factory)
}