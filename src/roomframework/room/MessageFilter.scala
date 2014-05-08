package roomframework.room

trait MessageFilter {
	def filter(msg: String): Option[String] = Some(msg)
}

