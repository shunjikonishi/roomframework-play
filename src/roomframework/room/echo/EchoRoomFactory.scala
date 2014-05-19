package roomframework.room.echo

import roomframework.room._

trait EchoRoomFactory extends RoomFactory {
  override def createHandler(room: Room) = new EchoRoomHandler(room)
}

class EchoRoomHandler(room: Room) extends RoomHandler(room) {
  override protected def handleMessage(msg: String): Unit = {
    room.channel.send(msg)
  }
}
