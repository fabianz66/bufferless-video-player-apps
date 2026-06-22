package engineer.zamora.bufferlessvideoplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onNavigateToPlayer: (String) -> Unit) {
    // This is Compose State. If urlText changes, the UI automatically recomposes!
    var urlText by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome to the Video App", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = urlText,
            onValueChange = { newText -> urlText = newText }, // Update state as user types
            label = { Text("Enter Video URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            // When clicked, we trigger the callback function passed from the NavHost
            onClick = { onNavigateToPlayer(urlText) },
            modifier = Modifier.fillMaxWidth(),
            enabled = urlText.isNotBlank() // Disable button if empty!
        ) {
            Text("Open Video Player")
        }
    }
}