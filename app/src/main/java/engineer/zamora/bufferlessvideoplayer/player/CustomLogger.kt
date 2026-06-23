package engineer.zamora.bufferlessvideoplayer.player

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface CustomLogger {
    val logs: StateFlow<List<String>>
    fun log(message: String, logCatOnly: Boolean = false)
}

class CustomLoggerImpl(
    private val tag: String = "CustomLogger",
    private val maxLogs: Int = 100
) : CustomLogger {
    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    override val logs: StateFlow<List<String>> = _logs

    override fun log(message: String, logCatOnly: Boolean) {
        if (logCatOnly) {
            Log.d(tag, message)
            return
        }
        val timestamp = timeFormatter.format(Date())
        val formattedMessage = "[$timestamp] $message"
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(0, formattedMessage) // Newest logs at the top
        if (currentLogs.size > maxLogs) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        _logs.value = currentLogs
    }
}
