package engineer.zamora.bufferlessvideoplayer.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

@UnstableApi
class CustomTrackSelector(context: android.content.Context) : DefaultTrackSelector(context) {
    init {
        // Encapsulate all your "Business Logic" for track picking here
        this.parameters = buildUponParameters()
            .setPreferredVideoMimeTypes(
                "video/av01",   // AV1
                "video/x-vnd.on2.vp9", // VP9
                "video/avc",    // H.264
            )
            // You can also add other encapsulated rules here
//            .setForceLowestBitrate(false)
            .setForceLowestBitrate(true)
            .setAllowVideoNonSeamlessAdaptiveness(true)
            .build()
    }
}