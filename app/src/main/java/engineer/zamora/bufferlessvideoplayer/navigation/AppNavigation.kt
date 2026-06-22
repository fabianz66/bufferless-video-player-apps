package engineer.zamora.bufferlessvideoplayer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import engineer.zamora.bufferlessvideoplayer.ui.screens.HomeScreen
import engineer.zamora.bufferlessvideoplayer.ui.screens.PlayerScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeRoute) {

        composable<HomeRoute> {
            HomeScreen(
                onNavigateToPlayer = { enteredUrl ->
                    navController.navigate(PlayerRoute(videoUrl = enteredUrl))
                }
            )
        }

        composable<PlayerRoute> { backStackEntry ->
            val routeData = backStackEntry.toRoute<PlayerRoute>()
            PlayerScreen(url = routeData.videoUrl)
        }
    }
}