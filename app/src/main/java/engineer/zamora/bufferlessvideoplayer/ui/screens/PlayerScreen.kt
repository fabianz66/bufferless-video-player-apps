package engineer.zamora.bufferlessvideoplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayerScreen(url: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Player Screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Now Playing: $url", color = MaterialTheme.colorScheme.primary)
    }
}