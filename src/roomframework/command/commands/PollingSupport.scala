package roomframework.command.commands

import akka.actor.Cancellable
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.Play.current
import roomframework.command._
import scala.concurrent.duration._
import scala.language.postfixOps

trait PollingSupport extends CommandInvoker {

  private var name = "noop"
  private var interval = 30
  private var schedule: Option[Cancellable] = None

  abstract override def onDisconnect: Unit = {
    stopPolling
    super.onDisconnect
  }

  def startPolling(interval: Int = interval, name: String = name) = {
    this.interval = interval
    this.name = name;

    schedule.foreach(_.cancel)
    val c = Akka.system.scheduler.schedule(interval seconds, interval seconds) {
      send(new CommandResponse(name, JsNull))
    }
    schedule = Some(c)
  }

  def stopPolling = {
    schedule.foreach(_.cancel)
    schedule = None    
  }

}

