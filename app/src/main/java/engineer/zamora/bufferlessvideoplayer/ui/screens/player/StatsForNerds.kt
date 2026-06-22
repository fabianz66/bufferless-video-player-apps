package engineer.zamora.bufferlessvideoplayer.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import engineer.zamora.bufferlessvideoplayer.player.PlaybackStats

@Composable
fun StatsForNerds(stats: PlaybackStats) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(8.dp)
    ) {
        StatRow(
            "State",
            stats.playerState,
            if (stats.playerState == "BUFFERING") Color.Yellow else Color.White
        )
        StatRow("Res", "${stats.resolution} @ ${stats.frameRate}")
        StatRow("Codec", stats.codec)
        StatRow("Decoder", stats.decoderName)
        StatRow("Bitrate", stats.bitrate)
        StatRow("Bandwidth", stats.bandwidth)
        StatRow(
            "Dropped",
            "${stats.droppedFrames}",
            if (stats.droppedFrames > 0) Color.Red else Color.White
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color = Color.White) {
    Row {
        Text(
            text = "$label: ",
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}
