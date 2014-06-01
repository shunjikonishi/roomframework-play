package roomframework.room

import java.nio.channels.ClosedChannelException
import play.api.Logger
import play.api.libs.iteratee.Iteratee
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import roomframework.command.CommandInvoker
import roomframework.command.CommandFilter
import roomframework.command.CommandResponse

class RoomHandler(room: Room) extends CommandInvoker {
  
  private var filterList: List[MessageFilter] = List.empty[MessageFilter]

  def broadcast(res: CommandResponse) = room.broadcast(res.copy(id=None).toString)
  
  def addBroadcastFilter(filter: MessageFilter) = {
    filterList = filter :: filterList
  }

  override protected def onDisconnect: Unit = {
    room.quit
  }

  private def filterBroadcast(msg: String): Option[String] = {
    def filterString(list: List[MessageFilter], msg: String): Option[String] = {
      if (list.size == 0) {
        Some(msg)
      } else {
        list.head.filter(msg) match {
          case Some(x) => filterString(list.tail, x)
          case None => None
        }
      }
    }
    def filterCommand(list: List[MessageFilter], msg: String, cmd: CommandResponse): Option[String] = {
      if (list.size == 0) {
        Some(msg)
      } else {
        list.head match {
          case f: CommandFilter =>
            f.filter(cmd) match {
              case Some(x) =>
                val newMsg = if (x == cmd) msg else x.toString
                filterCommand(list.tail, newMsg, x)
              case None => None
            }
          case f: MessageFilter =>
            f.filter(msg) match {
              case Some(x) =>
                val newCmd = if (x == msg) cmd else CommandResponse.fromJson(x)
                filterCommand(list.tail, x, newCmd)
              case None => None
            }
          case _ => throw new IllegalStateException()
        }
      }
    }
    try {
      val cmd = CommandResponse.fromJson(msg)
      filterCommand(filterList, msg, cmd)
    } catch {
      case e: Exception =>
        filterString(filterList.filter(_ match {
          case f: CommandFilter => false
          case _ => true
        }), msg)
    }
  }

  private def init = {
    val i = Iteratee.foreach[String] { msg =>
      filterBroadcast(msg).foreach { s =>
        try {
          channel.push(s.toString)
        } catch {
          case e: ClosedChannelException => 
            //Ignore
            Logger.info("Ignore closed channel")
          case e: Exception =>
            Logger.error("Error at broadcating message.")
            e.printStackTrace
        }
      }
    }
    room.channel.out(i)
  }
  init
}