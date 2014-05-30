package roomframework.command

import java.util.UUID
import roomframework.redis.RedisService
import play.api.cache._
import play.api.Play.current

trait AuthTokenProvider {
  def currentToken: String
  def nextToken: String
}

case class RedisTokenProvider(
  redis: RedisService, 
  sessionId: String, 
  expiration: Int = 60 * 60 * 2) extends AuthTokenProvider 
{
  def currentToken: String = {
    redis.get(sessionId + "#rfAuthToken").getOrElse(nextToken)
  }
  def nextToken: String = {
    val token = UUID.randomUUID().toString
    redis.setex(sessionId + "#rfAuthToken", expiration, token)
    token
  }
}

case class CacheTokenProvider(
  sessionId: String,
  expiration: Int = 60 * 60 * 2) extends AuthTokenProvider 
{
  def currentToken: String = {
    Cache.getAs[String](sessionId + "#rfAuthToken").getOrElse(nextToken)
  }
  def nextToken: String = {
    val token = UUID.randomUUID().toString
    Cache.set(sessionId + "#rfAuthToken", token, expiration)
    token
  }
}

trait AuthSupport extends CommandHandler { 
  self: CommandInvoker =>

  private var authCommands: List[String] = Nil
  private var authorized = false

  abstract override def handle(command: Command): CommandResponse = {
    if (authorized || authCommands.exists(_ == command.name)) {
      super.handle(command)
    } else { 
      command.error("Unauthorized")
    }
  }

  def addAuthTokenProvider(name: String, tokenProvider: AuthTokenProvider): Unit = {
    authCommands = name :: authCommands
    self.addHandler(name, new AuthCommand(tokenProvider))
  }

  def addAuthHandler(name: String)(handler: Command => (Boolean, CommandResponse)): Unit = {
    authCommands = name :: authCommands
    val h = CommandHandler { command =>
      val (ret, res) = handler(command)
      if (ret) {
        authorized = true
      }
      res
    }
    self.addHandler(name, h)
  }

  private class AuthCommand(tokenProvider: AuthTokenProvider) extends CommandHandler {
    override def handle(command: Command) = {
      val token = command.data.as[String]
      if (token == tokenProvider.currentToken) {
        authorized = true
        command.text(tokenProvider.nextToken)
      } else {
        command.error("Invalid authToken")
      }
    }
  }
}

