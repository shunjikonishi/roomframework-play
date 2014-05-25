package roomframework.command.commands

import play.api.Logger
import roomframework.command._

class LogCommand(prefix: String) extends CommandHandler {
  def this() = this("")

  def handle(command: Command): CommandResponse = {
    Logger.info(prefix + command.data.toString)
    CommandResponse.None
  }
}