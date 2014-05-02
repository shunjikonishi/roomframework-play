# RoomFramework-Play
This is a server side implementation of Room Framework.

## Install
Command:

    play new myapp
    cd myapp
    git submodule add https://github.com/shunjikonishi/roomframework-play.git app/roomframework

build.sbt:

    libraryDependencies ++= Seq(
      "net.debasishg" % "redisclient_2.10" % "2.11"
    )     
    
## Usage
    package controllers
    
    import play.api._
    import play.api.mvc._
    import flect.redis.RedisService
    
    object Application extends Controller {
    
      val myRedisService = RedisService("redis://@localhost:6379")
      
      def echo = WebSocket.using[String] { _ =>
        val channel = myRedisService.createPubSub("echo")
        (channel.in, channel.out)
      }
    }
