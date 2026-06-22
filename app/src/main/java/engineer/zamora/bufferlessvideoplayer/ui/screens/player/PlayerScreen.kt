package engineer.zamora.bufferlessvideoplayer.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView

@Composable
fun PlayerScreen(url: String, viewModel: PlayerViewModel = viewModel()) {
    val logs by viewModel.playerDebugger.logs.collectAsState()
    val stats by viewModel.playerDebugger.currentStats.collectAsState()

    // Tell the ViewModel to play the video when the screen first loads
    LaunchedEffect(url) {
        viewModel.playVideo(url)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.player
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            StatsForNerds(stats)
        }

        LogConsole(
            logs = logs,
            modifier = Modifier.weight(1f)
        )
    }
}
