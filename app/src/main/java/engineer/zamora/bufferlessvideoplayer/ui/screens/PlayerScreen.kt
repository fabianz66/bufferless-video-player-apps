package engineer.zamora.bufferlessvideoplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayerScreen(url: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Now Playing:", style = MaterialTheme.typography.labelLarge)
        Text(text = url, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
    }
}