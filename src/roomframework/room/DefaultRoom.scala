package roomframework.room

class DefaultRoom(val name: String) extends Room {
  val channel = new DefaultRoomChannel()
}

object DefaultRoom {
  def apply(name: String) = new DefaultRoom(name)
}

class DefaultRoomFactory extends RoomFactory {
  def createRoom(name: String) = new DefaultRoom(name)
}