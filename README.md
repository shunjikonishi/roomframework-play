# RoomFramework-Play
A framework for WebSocket application.
You can easily develop WebSocket application with this and [roomframework](https://github.com/shunjikonishi/roomframework) client library.

## Install
This library is not yet released.  
But you can use it with git submodule command.

Command:

    play new myapp
    cd myapp
    git submodule add https://github.com/shunjikonishi/roomframework-play.git app/roomframework

build.sbt:

    libraryDependencies ++= Seq(
      "net.debasishg" %% "redisclient" % "2.12"
    )     
    
## Usage
### CommandInvoker
CommandInvoker is a message router for roomframework message.

```scala
object Application extends Controller {

  def ws = WebSocket.using[String] { _ =>
    val ci = new CommandInvoker()
    ci.addHandler("hello") { command =>
      val msg = command.data.as[String]
      command.text("Hello " + msg)
    }
    ci.addHandler("add", new AddCommand())
    (ci.in, ci.out)
  }
  class AddCommand extends CommandHandler {
    def handle(command: Command): CommandResponse = {
      val a = (command \ "a").as[Int]
      val b = (command \ "b").as[Int]
      command.json(JsNumber(a + b))
    }
  }
}

```

### RoomModel
RoomHandler is an implementation of CommandInvoker with room.
You can broadcast a message to same room members.

```scala
object Application extends Controller {

  val rm = RoomManager(new DefaultRoomFactory())

  def ws(roomName: String) = WebSocket.using[String] { implicit request =>
    val room = rm.join(roomName)
    val handler = new RoomHandler(roomName)
    handler.addHandler("chat") { command =>
      broadcast(command.json(command.data))
      CommandResponse.None
    }
    (handler.in, handler.out)
  }

}
```

If you want to scale out, you can use RedisRoom.

```scala
object Application extends Controller {

  val redis = RedisService("redis://@localhost:6379")
  val rm = RoomManager(new RedisRoomFactory(redis))

  def ws(roomName: String) = WebSocket.using[String] { implicit request =>
    val room = rm.join(roomName)
    val handler = new RoomHandler(roomName)
    handler.addHandler("chat") { command =>
      broadcast(command.json(command.data))
      CommandResponse.None
    }
    (handler.in, handler.out)
  }

}
```

### AuthSupport
Onetime token authentication support.

```scala
object Application extends Controller {

  //HTML page which contains ws connecting script.
  def index = Action { implicit request =>
    val sid = session.get("sessionId").getOrElse(UUID.randomUUID.toString)
    val token = CacheTokenProvider(sid).nextToken
    Ok(views.html.index(token)).withSession(
      "sessionId" -> sid
    )
  }

  def ws = WebSocket.using[String] { implicit request =>
    val sid = request.session("sessionId")
    val ci = new CommandInvoker() with AuthSupport
    ci.addAuthTokenProvider("room.auth", CacheTokenProvider(sid))
    (ci.in, ci.out)
  }

}
```

```javascript
$(function() {
  var token = "@token",
    url = "ws://" + location.host + "/ws",
    ws = new room.Connection({
      url: url,
      authToken: token,
      authCommand: "room.auth"
    });
});
```