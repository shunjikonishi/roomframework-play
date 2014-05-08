package roomframework.command

import roomframework.room.MessageFilter

trait CommandFilter extends MessageFilter {
  override final def filter(msg: String): Option[String] = {
    try {
      val cmd = CommandResponse.fromJson(msg)
      filter(cmd).map(_.toString)
    } catch {
      case e: Exception =>
        Some(msg)
    }
  }
  def filter(msg: CommandResponse): Option[CommandResponse]
}