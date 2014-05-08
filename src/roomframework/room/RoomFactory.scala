package roomframework.room

trait RoomFactory {
  def createRoom(name: String): Room
  def createHandler(room: Room): RoomHandler = new RoomHandler(room) 
}

