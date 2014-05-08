package roomframework.command

trait CommandBroadcast {

	def send(res: CommandResponse): Unit
}