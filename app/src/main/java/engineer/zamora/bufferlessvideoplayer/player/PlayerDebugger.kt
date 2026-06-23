package engineer.zamora.bufferlessvideoplayer.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.PlaybackException
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
import java.util.Locale

/**
 * Data class representing current playback metrics.
 */
data class PlaybackStats(
    val resolution: String = "Unknown",
    val bitrate: String = "0 bps",
    val bandwidth: String = "0 bps",
    val codec: String = "Unknown",
    val playerState: String = "IDLE",
    val frameRate: String = "0 fps",
    val decoderName: String = "Unknown",
    val droppedFrames: Int = 0,
    val bufferDuration: String = "0s"
)

/**
 * Monitors and logs playback events, tracks, and performance metrics from [ExoPlayer].
 */
@UnstableApi
class PlayerDebugger(
    private val logger: CustomLogger
) : Player.Listener, AnalyticsListener {

    private var attachedPlayer: ExoPlayer? = null
    private val _currentStats = MutableStateFlow(PlaybackStats())

    /** Current real-time playback statistics. */
    val currentStats: StateFlow<PlaybackStats> = _currentStats.asStateFlow()

    /**
     * Attaches the debugger to the player and starts monitoring.
     */
    fun startDebugging(exoPlayer: ExoPlayer) {
        attachedPlayer = exoPlayer
        log("Debugging started")
        // Monitor tracks, codecs, and basic player events
        exoPlayer.addListener(this)
        // Monitor detailed loading and bandwidth analytics
        exoPlayer.addAnalyticsListener(this)
    }

    // region Player.Listener Implementation

    override fun onPlaybackStateChanged(playbackState: Int) {
        updatePlayerState()
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        updatePlayerState()
    }

    override fun onPlayerError(error: PlaybackException) {
        log("Error: ${error.message}")
    }

    override fun onTracksChanged(tracks: Tracks) {
        log("Tracks Changed. Groups: ${tracks.groups.size}")

        var videoRes = "Unknown"
        var videoBitrate = "0 bps"
        var videoCodec = "Unknown"
        var fps = "Unknown"

        tracks.groups.forEach { group ->
            if (group.type == C.TRACK_TYPE_VIDEO && group.isSelected) {
                for (i in 0 until group.length) {
                    if (group.isTrackSelected(i)) {
                        val format = group.getTrackFormat(i)
                        videoRes = "${format.width}x${format.height}"
                        videoBitrate = "${format.bitrate / 1000} kbps"
                        videoCodec = format.sampleMimeType ?: "Unknown"
                        fps =
                            if (format.frameRate > 0) "${format.frameRate.toInt()} fps" else "Unknown"
                    }
                }
            }
        }

        _currentStats.value = _currentStats.value.copy(
            resolution = videoRes,
            bitrate = videoBitrate,
            codec = videoCodec,
            frameRate = fps
        )

        // Log detailed track information
        tracks.groups.forEachIndexed { _, group ->
            val type = when (group.type) {
                C.TRACK_TYPE_AUDIO -> "A"
                C.TRACK_TYPE_TEXT -> "T"
                C.TRACK_TYPE_VIDEO -> "V"
                else -> "Other (${group.type})"
            }
            for (i in 0 until group.length) {
                val format: Format = group.getTrackFormat(i)
                val isSelected = group.isTrackSelected(i)
                log("Track[$type]: ${format.width}x${format.height}, ${format.bitrate}bps, ${format.sampleMimeType}, selected=$isSelected")
            }
        }
    }

    /**
     * Called when one or more player events occur.
     *
     * This implementation monitors for dropped video frames via the decoder counters
     * and updates the [currentStats] accordingly. It also detects when a new media
     * manifest has been loaded by checking for timeline changes.
     *
     * @param player The [Player] emitting the events.
     * @param events The set of [Player.Events] that occurred.
     */
    @OptIn(UnstableApi::class)
    override fun onEvents(player: Player, events: Player.Events) {
        // Monitor dropped frames and buffer periodically
        attachedPlayer?.let { exo ->
            val counters = exo.videoDecoderCounters
            val dropped = counters?.droppedBufferCount ?: 0

            val bufferMs = (exo.bufferedPosition - exo.currentPosition).coerceAtLeast(0)
            val bufferSeconds = String.format(Locale.getDefault(), "%.1fs", bufferMs / 1000.0)

            if (dropped != _currentStats.value.droppedFrames || bufferSeconds != _currentStats.value.bufferDuration) {
                _currentStats.value = _currentStats.value.copy(
                    droppedFrames = dropped,
                    bufferDuration = bufferSeconds
                )
            }
        }

        if (events.contains(Player.EVENT_TIMELINE_CHANGED)) {
            if (player.currentManifest != null) {
                log("Manifest loaded")
            }
        }
    }

    // endregion

    // region AnalyticsListener Implementation

    override fun onVideoDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        log("Decoder Initialized: $decoderName")
        _currentStats.value = _currentStats.value.copy(decoderName = decoderName)
    }

    override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
        log("Error: ${error.message}")
    }

    @OptIn(UnstableApi::class)
    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        logger.log("Chunk S: ${loadEventInfo.uri.lastPathSegment ?: "unknown"}", logCatOnly = true)
    }

    @OptIn(UnstableApi::class)
    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData
    ) {
        logger.log("Chunk C: ${loadEventInfo.uri.lastPathSegment ?: "unknown"}", logCatOnly = true)
    }

    override fun onBandwidthEstimate(
        eventTime: AnalyticsListener.EventTime,
        totalLoadTimeMs: Int,
        totalBytesLoaded: Long,
        bitrateEstimate: Long
    ) {
        val bandwidthKbps = bitrateEstimate / 1000
        val bandwidthString = if (bandwidthKbps > 1000) {
            String.format(Locale.getDefault(), "%.2f Mbps", bandwidthKbps / 1000.0)
        } else {
            "$bandwidthKbps kbps"
        }
        _currentStats.value = _currentStats.value.copy(bandwidth = bandwidthString)
    }

    // endregion

    // region Private Helper Methods

    /**
     * Updates the player state string (PLAYING, PAUSED, BUFFERING, etc.)
     */
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

    /**
     * Helper to send log messages to the [CustomLogger].
     */
    private fun log(message: String) {
        logger.log(message)
    }

    // endregion
}
