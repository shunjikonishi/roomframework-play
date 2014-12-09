package roomframework.room

trait RoomFactory[T <: Room] {
  def createRoom(name: String): T
}

