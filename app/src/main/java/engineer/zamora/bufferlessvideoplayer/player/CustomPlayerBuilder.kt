package engineer.zamora.bufferlessvideoplayer.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter

@UnstableApi
class CustomPlayerBuilder(
    private val context: Context,
    private val logger: CustomLogger
) {

    private val bandwidthMeter = DefaultBandwidthMeter.getSingletonInstance(context)

    // 1. Create the ABR Factory
    private val trackSelectionFactory = CustomTrackSelectionFactory(bandwidthMeter, logger)

    // 2. Create the Track Selector using our custom ABR factory
    private val trackSelector = CustomTrackSelector(context, trackSelectionFactory)

    // 3. Create the Load Control
    private val loadControl = CustomLoadControl()

    // 4. Create the Debugger
    val playerDebugger = PlayerDebugger(logger)

    fun build(): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
//            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .build()
            .apply {
                playerDebugger.startDebugging(this)
            }
    }
}
