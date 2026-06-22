package engineer.zamora.bufferlessvideoplayer.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import engineer.zamora.bufferlessvideoplayer.ui.screens.HomeScreen
import engineer.zamora.bufferlessvideoplayer.ui.screens.PlayerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val title = when {
        currentDestination?.hasRoute<HomeRoute>() == true -> "Home"
        currentDestination?.hasRoute<PlayerRoute>() == true -> "Video Player"
        else -> "Bufferless Player"
    }

    val canNavigateBack = navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding)
        ) {

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
}
