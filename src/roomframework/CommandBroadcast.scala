package roomframework

trait CommandBroadcast {

	def send(res: CommandResponse): Unit
}