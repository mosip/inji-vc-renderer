package io.mosip.injivcrenderer

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Locale

object DateUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd"

    // Method to format date
    fun formatDate(dateString: String): String {
        val date =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(dateString)
        if (date != null) {
            val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            return formatter.format(date)
        }
        return ""
    }

    // Method to check if date is valid
    fun isValidDateTime(dateString: String): Boolean {
        return try {
            val date =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(dateString)
            date != null && date.year in 0..9999
        } catch (e: Exception) {
            false
        }
    }
}