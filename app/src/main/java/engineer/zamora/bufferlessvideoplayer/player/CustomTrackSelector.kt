package engineer.zamora.bufferlessvideoplayer.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.ExoTrackSelection

/**
 * This class selects the allowed tracks, i.e. what tracks the ABR logic can pick from.
 */
@UnstableApi
class CustomTrackSelector(
    context: android.content.Context,
    trackSelectionFactory: ExoTrackSelection.Factory
) : DefaultTrackSelector(context, trackSelectionFactory) {
    init {
        this.parameters = buildUponParameters()
            .setPreferredVideoMimeTypes(
                "video/av01",   // AV1
                "video/x-vnd.on2.vp9", // VP9
                "video/avc",    // H.264
            )
            .setAllowVideoNonSeamlessAdaptiveness(true)
            .build()
    }
}