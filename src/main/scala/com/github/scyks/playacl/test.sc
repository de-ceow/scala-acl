import java.sql.Timestamp
import java.util.Calendar

object test {
	val cal = Calendar.getInstance()
	val now = new Timestamp(cal.getTimeInMillis - 300000)


}