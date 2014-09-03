package roomframework.room.echo

import roomframework.room._

class EchoRoomHandler(room: Room) extends RoomHandler(room) {
  override protected def handleMessage(msg: String): Unit = {
    room.channel.send(msg)
  }
}
