package engineer.zamora.bufferlessvideoplayer.navigation

import kotlinx.serialization.Serializable

// The Home screen doesn't need to receive any data.
@Serializable
data object HomeRoute

// The Player screen needs to receive the URL string.
@Serializable
data class PlayerRoute(val videoUrl: String)