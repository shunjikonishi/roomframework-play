package roomframework.room

import akka.actor.Actor
import akka.actor.Props
import akka.actor.PoisonPill
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.Await
import play.api.Play.current
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Enumerator
import play.api.libs.concurrent.Akka

trait Room {

  implicit val timeout = Timeout(5)

  val name: String
  val channel: RoomChannel

  private var connectCount = 0
  private var active = true
  private val actor = Akka.system.actorOf(Props(new MyActor()))


  def isActive = active
  def isClosed = !active
  def memberCount = connectCount
  def broadcast(msg: String) = channel.send(msg)

  private sealed class RoomMessage
  private case object RoomConnect extends RoomMessage
  private case object RoomDisconnect extends RoomMessage
  private case object RoomClose extends RoomMessage
  
  private class MyActor extends Actor {
    def receive = {
      case RoomConnect =>
        connectCount += 1
        sender ! connectCount
      case RoomDisconnect =>
        if (isActive && connectCount > 0) {
          connectCount -= 1
          if (connectCount == 0) {
            self ! RoomClose
          }
        }
        sender ! connectCount
      case RoomClose =>
        active = false
        connectCount = 0
        channel.close
    }
  }
  
  def join: Int = {
    if (isClosed) {
      throw new IllegalStateException("Room %s already closed.".format(name))
    }
    val ret = (actor ? RoomConnect).asInstanceOf[Future[Int]]
    Await.result(ret, Duration.Inf)
  }
  def quit: Int = {
    val ret = (actor ? RoomDisconnect).asInstanceOf[Future[Int]]
    Await.result(ret, Duration.Inf)
  }
  
  def close = {
    actor ! RoomClose
    actor ! PoisonPill
  }
}

