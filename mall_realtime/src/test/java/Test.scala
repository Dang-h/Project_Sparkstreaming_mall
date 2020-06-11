import java.text.SimpleDateFormat
import java.util.Date

object Test {
	def main(args: Array[String]): Unit = {
		val ts:Long = 1591784817763L
		println(timestamp2Date(ts))
	}

	def timestamp2Date(ts: Long): String = {
		val date: String = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date(ts))
		date
	}
}
