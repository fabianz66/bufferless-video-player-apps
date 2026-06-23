package engineer.zamora.bufferlessvideoplayer.player

import androidx.media3.common.C
import androidx.media3.common.TrackGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.chunk.MediaChunk
import androidx.media3.exoplayer.source.chunk.MediaChunkIterator
import androidx.media3.exoplayer.trackselection.BaseTrackSelection
import androidx.media3.exoplayer.upstream.BandwidthMeter

/**
 * This class contains the ABR logic.
 */
@UnstableApi
class CustomTrackSelection(
    group: TrackGroup,
    tracks: IntArray,
    private val bandwidthMeter: BandwidthMeter,
    private val logger: CustomLogger? = null
) : BaseTrackSelection(group, *tracks) {

    private var selectedIndex = 0
    private var selectionReason = C.SELECTION_REASON_INITIAL

    override fun updateSelectedTrack(
        playbackPositionUs: Long,
        bufferedDurationUs: Long,
        availableDurationUs: Long,
        queue: List<MediaChunk>,
        mediaChunkIterators: Array<out MediaChunkIterator>
    ) {
        // --- THIS IS THE BRAIN OF THE ABR ---

        val currentBandwidth = bandwidthMeter.bitrateEstimate

        val trackInfo = (0 until length).joinToString(", ") { i ->
            val f = getFormat(i)
            "Track: ${f.codecs}[${f.height}]"
        }

        logger?.log(
            "ABR Update | P: ${playbackPositionUs / 1000}ms | " +
                    "R: ${bufferedDurationUs / 1000}ms | BW: ${currentBandwidth}bps | " +
                    "G: ${group.id} | T: [$trackInfo]", logCatOnly = true
        )

        // Example Logic: If buffer is low, force the lowest bitrate immediately
        if (bufferedDurationUs < 5_000_000) { // Less than 5 seconds
            if (selectedIndex != 0) {
                logger?.log("Low buffer (${bufferedDurationUs / 1000}ms). Switching to lowest bitrate.")
            }
            selectedIndex = 0 // Pick the lowest bitrate track
            selectionReason = C.SELECTION_REASON_ADAPTIVE
        } else {
            // Normal logic: pick based on bandwidth
            // (You would iterate through tracks and compare bitrates to currentBandwidth)
        }
    }

    override fun getSelectedIndex(): Int = selectedIndex
    override fun getSelectionReason(): Int = selectionReason
    override fun getSelectionData(): Any? = null
}
