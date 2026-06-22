package engineer.zamora.bufferlessvideoplayer.player

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PlaybackStats(
    val resolution: String = "Unknown",
    val bitrate: String = "0 bps",
    val codec: String = "Unknown",
    val playerState: String = "IDLE",
    val frameRate: String = "0 fps",
    val decoderName: String = "Unknown",
    val droppedFrames: Int = 0
)

@UnstableApi
class PlayerDebugger : Player.Listener, AnalyticsListener {
    private val TAG = "PlayerDebugger"
    private val timeFormatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private var attachedPlayer: ExoPlayer? = null

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    private val _currentStats = MutableStateFlow(PlaybackStats())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()
    val currentStats: StateFlow<PlaybackStats> = _currentStats.asStateFlow()

    private fun log(message: String) {
        val timestamp = timeFormatter.format(Date())
        val formattedMessage = "[$timestamp] $message"
        Log.d(TAG, message)
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(0, formattedMessage) // Newest logs at the top
        if (currentLogs.size > 100) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        _logs.value = currentLogs
    }

    fun startDebugging(exoPlayer: ExoPlayer) {
        attachedPlayer = exoPlayer
        log("Debugging started")
        // 1. Monitor Tracks, Codecs, and Resolutions
        exoPlayer.addListener(this)
        // 2. Monitor Segment Downloads
        exoPlayer.addAnalyticsListener(this)
    }

    override fun onVideoDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        log("Decoder Initialized: $decoderName")
        _currentStats.value = _currentStats.value.copy(decoderName = decoderName)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        updatePlayerState()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        updatePlayerState()
    }

    private fun updatePlayerState() {
        val player = attachedPlayer ?: return
        val playbackState = player.playbackState
        val playWhenReady = player.playWhenReady

        val stateString = when (playbackState) {
            Player.STATE_IDLE -> "IDLE"
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_READY -> if (playWhenReady) "PLAYING" else "PAUSED"
            Player.STATE_ENDED -> "ENDED"
            else -> "UNKNOWN"
        }
        
        if (stateString != _currentStats.value.playerState) {
            log("State: $stateString")
            _currentStats.value = _currentStats.value.copy(playerState = stateString)
        }
    }

    override fun onTracksChanged(tracks: Tracks) {
        log("Tracks Changed. Groups: ${tracks.groups.size}")

        var videoRes = "Unknown"
        var videoBitrate = "0 bps"
        var videoCodec = "Unknown"

        tracks.groups.forEach { group ->
            if (group.type == C.TRACK_TYPE_VIDEO && group.isSelected) {
                for (i in 0 until group.length) {
                    if (group.isTrackSelected(i)) {
                        val format = group.getTrackFormat(i)
                        videoRes = "${format.width}x${format.height}"
                        videoBitrate = "${format.bitrate / 1000} kbps"
                        videoCodec = format.sampleMimeType ?: "Unknown"
                    }
                }
            }
        }

        _currentStats.value = _currentStats.value.copy(
            resolution = videoRes,
            bitrate = videoBitrate,
            codec = videoCodec
        )

        tracks.groups.forEachIndexed { index, group ->
            val type = when (group.type) {
                C.TRACK_TYPE_AUDIO -> "Audio"
                C.TRACK_TYPE_TEXT -> "Text"
                C.TRACK_TYPE_VIDEO -> "Video"
                else -> "Other (${group.type})"
            }
            for (i in 0 until group.length) {
                val format: Format = group.getTrackFormat(i)
                val isSelected = group.isTrackSelected(i)

                if (isSelected) {
                    val fps =
                        if (format.frameRate > 0) "${format.frameRate.toInt()} fps" else "Unknown"
                    _currentStats.value = _currentStats.value.copy(frameRate = fps)
                }

                log("  $type track $i: ${format.width}x${format.height}, ${format.bitrate}bps, ${format.sampleMimeType}, selected=$isSelected")
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onEvents(player: Player, events: Player.Events) {
        // Polling logic for dropped frames (called frequently by the player)
        attachedPlayer?.let { exo ->
            val counters = exo.videoDecoderCounters
            if (counters != null) {
                val dropped = counters.droppedBufferCount
                if (dropped != _currentStats.value.droppedFrames) {
                    _currentStats.value = _currentStats.value.copy(droppedFrames = dropped)
                }
            }
        }

        if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
            val manifest = player.currentManifest
            if (manifest != null) {
                log("Manifest loaded")
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        log("Download Started: ${loadEventInfo.uri.lastPathSegment ?: "unknown"}")
    }

    @OptIn(UnstableApi::class)
    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        log("Download Completed: ${loadEventInfo.uri.lastPathSegment ?: "unknown"} (${loadEventInfo.bytesLoaded} bytes in ${loadEventInfo.loadDurationMs}ms)")
    }
}
