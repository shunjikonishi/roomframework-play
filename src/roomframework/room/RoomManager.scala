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
import scala.language.postfixOps
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

class RoomManager[T <: Room](factory: RoomFactory[T]) {
  
  implicit val timeout = Timeout(5 seconds)
  private var rooms = Map.empty[String, T]
  private val actor = Akka.system.actorOf(Props(new MyActor()))
  
  def join(room: String): T = {
    val ret = (actor ? Join(room)).asInstanceOf[Future[T]]
    Await.result(ret, Duration.Inf)
  }
  
  protected def terminate() = {
    rooms.values.filter(_.isActive).foreach(_.close)
    rooms = Map.empty[String, T]
  }
  
  private def getRoom(name: String): T = {
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
        sender ! room
    }
    
    override def postStop() = {
      terminate()
      super.postStop()
    }
  }
  
} 

object RoomManager {
  def apply[T <: Room](factory: RoomFactory[T]) = new RoomManager(factory)
}