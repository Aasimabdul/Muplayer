package com.example.muplayer.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.muplayer.ui.theme.liquidGlass
import com.example.muplayer.ui.theme.liquidGlassTab
import com.example.muplayer.ui.components.MiniPlayer
import com.example.muplayer.viewmodel.MusicViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Songs : Screen("songs", "Songs", Icons.Filled.MusicNote)
    object Folders : Screen("folders", "Folders", Icons.Filled.Folder)
    object Playlists : Screen("playlists", "Playlists", Icons.Filled.QueueMusic)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun MainScreen(
    viewModel: MusicViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Songs, Screen.Folders, Screen.Playlists, Screen.Settings)
    
    val currentTrack by viewModel.currentTrack.collectAsState()
    var showNowPlaying by remember { mutableStateOf(false) }

    if (showNowPlaying && currentTrack != null) {
        NowPlayingScreen(viewModel, onBack = { showNowPlaying = false })
    } else {
        Scaffold(
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassTab(
                                shape = RoundedCornerShape(24.dp),
                                accentColor = MaterialTheme.colorScheme.primary
                            )
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        items.forEach { screen ->
                            // Keep Settings highlighted when its nested Statistics screen is open.
                            val isSelected = currentRoute == screen.route || (screen == Screen.Settings && currentRoute == "stats")
                            NavigationBarItem(
                                icon = {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected)
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                                else
                                                    Color.Transparent
                                            )
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            // Use navigation insets properly, removing arbitrary hardcoded background
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                NavigationGraph(
                    navController, viewModel, hasPermission, onRequestPermission,
                    modifier = Modifier.fillMaxSize().padding(bottom = if (currentTrack != null) 88.dp else 0.dp)
                )
                
                // Floating Mini Player
                AnimatedVisibility(
                    visible = currentTrack != null,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    currentTrack?.let { track ->
                        MiniPlayer(
                            track = track,
                            viewModel = viewModel,
                            onClick = { showNowPlaying = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController, 
    viewModel: MusicViewModel,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                hasPermission = hasPermission,
                onRequestPermission = onRequestPermission,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Songs.route) {
            LibraryScreen(viewModel, hasPermission, onRequestPermission)
        }
        composable(Screen.Folders.route) {
            FolderScreen(viewModel, hasPermission, onRequestPermission)
        }
        composable(Screen.Playlists.route) {
            PlaylistsScreen(viewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel, onNavigateToStats = { navController.navigate("stats") })
        }
        composable("stats") {
            StatsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
