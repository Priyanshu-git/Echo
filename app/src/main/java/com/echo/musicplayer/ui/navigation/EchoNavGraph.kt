package com.echo.musicplayer.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.echo.musicplayer.ui.app.EchoAppViewModel
import com.echo.musicplayer.ui.components.MiniPlayer
import com.echo.musicplayer.ui.screens.AboutScreen
import com.echo.musicplayer.ui.screens.DownloadAllScreen
import com.echo.musicplayer.ui.screens.DownloadsScreen
import com.echo.musicplayer.ui.screens.FavoritesScreen
import com.echo.musicplayer.ui.screens.LibraryScreen
import com.echo.musicplayer.ui.screens.NowPlayingScreen
import com.echo.musicplayer.ui.screens.QueueScreen
import com.echo.musicplayer.ui.screens.SearchScreen
import com.echo.musicplayer.ui.screens.SettingsScreen
import com.echo.musicplayer.ui.screens.SongOptionsScreen
import com.echo.musicplayer.ui.screens.StorageScreen

private data class MainTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val mainTabs = listOf(
    MainTab("library", "Library", Icons.Filled.LibraryMusic),
    MainTab("downloads", "Downloads", Icons.Filled.Download),
    MainTab("favorites", "Favorites", Icons.Filled.Favorite),
    MainTab("settings", "Settings", Icons.Filled.Settings),
)

@Composable
fun EchoNavGraph() {
    val navController = rememberNavController()
    val viewModel: EchoAppViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "library",
        modifier = Modifier.fillMaxSize(),
    ) {
        composable("library") {
            MainScaffold(navController, "library", state, viewModel::togglePlayPause) { padding ->
                LibraryScreen(
                    state = state,
                    contentPadding = padding,
                    onSearch = { navController.navigate("search") },
                    onSongClick = viewModel::play,
                    onMore = { navController.navigate("songOptions/${it.id}") },
                )
            }
        }
        composable("downloads") {
            MainScaffold(navController, "downloads", state, viewModel::togglePlayPause) { padding ->
                DownloadsScreen(
                    state = state,
                    contentPadding = padding,
                    onSearch = { navController.navigate("search") },
                    onDownload = viewModel::download,
                    onDownloadAll = {
                        viewModel.downloadAll()
                        navController.navigate("downloadAll")
                    },
                )
            }
        }
        composable("favorites") {
            MainScaffold(navController, "favorites", state, viewModel::togglePlayPause) { padding ->
                FavoritesScreen(
                    state = state,
                    contentPadding = padding,
                    onSearch = { navController.navigate("search") },
                    onSongClick = viewModel::play,
                    onFavorite = viewModel::toggleFavorite,
                )
            }
        }
        composable("settings") {
            MainScaffold(navController, "settings", state, viewModel::togglePlayPause) { padding ->
                SettingsScreen(
                    state = state,
                    contentPadding = padding,
                    onColor = viewModel::setPrimaryColor,
                    onThemeMode = viewModel::setThemeMode,
                    onKeepScreen = viewModel::setKeepScreenOnWhilePlaying,
                    onStorage = { navController.navigate("storage") },
                    onAbout = { navController.navigate("about") },
                )
            }
        }
        composable("search") {
            SearchScreen(
                state = state,
                onBack = navController::popBackStack,
                onQuery = viewModel::setSearchQuery,
                onSongClick = viewModel::play,
            )
        }
        composable("nowPlaying") {
            NowPlayingScreen(
                state = state,
                onBack = navController::popBackStack,
                onToggle = viewModel::togglePlayPause,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                onSeek = viewModel::seekTo,
                onFavorite = { state.playback.currentSong?.let(viewModel::toggleFavorite) },
                onDownload = { state.playback.currentSong?.let(viewModel::download) },
                onQueue = { navController.navigate("queue") },
            )
        }
        composable("queue") {
            QueueScreen(
                state = state,
                onBack = navController::popBackStack,
                onPlay = viewModel::play,
                onRemove = viewModel::removeFromQueue,
                onClear = viewModel::clearQueue,
            )
        }
        composable("storage") {
            StorageScreen(
                state = state,
                onBack = navController::popBackStack,
                onClearDownloads = viewModel::clearDownloads,
                onClearCache = viewModel::clearCache,
            )
        }
        composable("about") {
            AboutScreen(onBack = navController::popBackStack)
        }
        composable("downloadAll") {
            DownloadAllScreen(
                state = state,
                onBack = navController::popBackStack,
                onCancel = viewModel::cancelDownloads,
            )
        }
        composable(
            route = "songOptions/{songId}",
            arguments = listOf(navArgument("songId") { type = NavType.StringType }),
        ) { entry ->
            val song = state.songs.firstOrNull { it.id == entry.arguments?.getString("songId") }
            SongOptionsScreen(
                song = song,
                onBack = navController::popBackStack,
                onPlayNext = {
                    song?.let(viewModel::play)
                    navController.popBackStack()
                },
                onFavorite = {
                    song?.let(viewModel::toggleFavorite)
                    navController.popBackStack()
                },
                onDownload = {
                    song?.let(viewModel::download)
                    navController.popBackStack()
                },
                onDeleteDownload = {
                    song?.let(viewModel::deleteDownload)
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    selectedRoute: String,
    state: com.echo.musicplayer.ui.app.EchoAppUiState,
    onTogglePlayPause: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: selectedRoute

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                MiniPlayer(
                    playback = state.playback,
                    onOpen = { navController.navigate("nowPlaying") },
                    onToggle = onTogglePlayPause,
                )
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    mainTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo("library") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding -> content(padding) }
}
