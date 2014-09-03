package roomframework.room

trait RoomFactory {
  def createRoom(name: String): Room
}

