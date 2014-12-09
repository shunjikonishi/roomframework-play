package roomframework.redis

import roomframework.room.Room
import roomframework.room.RoomFactory

class RedisRoom(val name: String, redis: RedisService) extends Room {

  val channel = redis.createPubSub(name)
  
}

object RedisRoom {
  def factory(redis: RedisService) = new RedisRoomFactory(redis)
}

class RedisRoomFactory(redis: RedisService) extends RoomFactory[RedisRoom] {
  def createRoom(name: String) = new RedisRoom(name, redis)
}

