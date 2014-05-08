package roomframework.room

import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Enumerator

trait RoomChannel {
  def send(msg: String): Unit
  val out: Enumerator[String]
  def close = {}
}

class DefaultRoomChannel extends RoomChannel {
  private val (e, channel) = Concurrent.broadcast[String]

  def send(msg: String) = channel.push(msg)
  val out = e 

}