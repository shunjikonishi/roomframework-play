package roomframework.redis

import org.apache.commons.pool._
import org.apache.commons.pool.impl._
import com.redis.cluster.ClusterNode
import com.redis._

/**
 * Following classes are copied from com.redis.Pool.scala
 * Changes from original are follows.
 * - StackObjectPool -> GeneralObjectPool
 */
class RedisClientFactoryEx(val host: String, val port: Int, val database: Int = 0, val secret: Option[Any] = None) 
  extends PoolableObjectFactory[RedisClient] {

  // when we make an object it's already connected
  def makeObject = {
    new RedisClient(host, port, database, secret)
  }

  // quit & disconnect
  def destroyObject(rc: RedisClient): Unit = {
    rc.quit // need to quit for closing the connection
    rc.disconnect // need to disconnect for releasing sockets
  }

  // noop: we want to have it connected
  def passivateObject(rc: RedisClient): Unit = {}
  def validateObject(rc: RedisClient) = rc.connected == true

  // noop: it should be connected already
  def activateObject(rc: RedisClient): Unit = {}
}

class RedisClientPoolEx(val host: String, val port: Int, val maxIdle: Int = 8, val database: Int = 0, val secret: Option[Any] = None, maxActive: Int = 0) {
  val pool = {
    val factory = new RedisClientFactoryEx(host, port, database, secret)
    if (maxActive == 0) {
      new StackObjectPool(factory, maxIdle)
    } else {
      new GenericObjectPool(factory, maxActive, GenericObjectPool.WHEN_EXHAUSTED_BLOCK, -1, maxIdle)
    }
  }
  override def toString = host + ":" + String.valueOf(port)

  def withClient[T](body: RedisClient => T) = {
    val client = pool.borrowObject
    try {
      body(client)
    } finally {
      pool.returnObject(client)
    }
  }

  // close pool & free resources
  def close = pool.close
}

