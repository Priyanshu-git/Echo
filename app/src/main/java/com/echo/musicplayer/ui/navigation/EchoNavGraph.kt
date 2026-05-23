package com.echo.musicplayer.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.echo.musicplayer.ui.screens.UploadScreen

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

    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            MainScaffold(navController, "library") { padding ->
                LibraryScreen(
                    state = state,
                    contentPadding = padding,
                    onSearch = { navController.navigate("search") },
                    onUpload = { navController.navigate("upload") },
                    onSongClick = viewModel::play,
                    onMore = { navController.navigate("songOptions/${it.id}") },
                    onNowPlaying = { navController.navigate("nowPlaying") },
                    onTogglePlayback = viewModel::togglePlayPause,
                )
            }
        }
        composable("downloads") {
            MainScaffold(navController, "downloads") { padding ->
                DownloadsScreen(
                    state = state,
                    contentPadding = padding,
                    onSearch = { navController.navigate("search") },
                    onDownload = viewModel::download,
                    onDownloadAll = {
                        viewModel.downloadAll()
                        navController.navigate("downloadAll")
                    },
                    onNowPlaying = { navController.navigate("nowPlaying") },
                    onTogglePlayback = viewModel::togglePlayPause,
                )
            }
        }
        composable("favorites") {
            MainScaffold(navController, "favorites") { padding ->
                FavoritesScreen(
                    state = state,
                    contentPadding = padding,
                    onSearch = { navController.navigate("search") },
                    onSongClick = viewModel::play,
                    onFavorite = viewModel::toggleFavorite,
                    onNowPlaying = { navController.navigate("nowPlaying") },
                    onTogglePlayback = viewModel::togglePlayPause,
                )
            }
        }
        composable("settings") {
            MainScaffold(navController, "settings") { padding ->
                SettingsScreen(
                    state = state,
                    contentPadding = padding,
                    onColor = viewModel::setPrimaryColor,
                    onWifi = viewModel::setDownloadOverWifiOnly,
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
        composable("upload") {
            UploadScreen(
                state = state,
                onBack = navController::popBackStack,
                onUpload = viewModel::uploadSampleSong,
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
                onPlayNext = { song?.let(viewModel::play) },
                onFavorite = { song?.let(viewModel::toggleFavorite) },
                onDownload = { song?.let(viewModel::download) },
                onDeleteDownload = { song?.let(viewModel::deleteDownload) },
            )
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    selectedRoute: String,
    content: @Composable (PaddingValues) -> Unit,
) {
    val viewModel: EchoAppViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: selectedRoute

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                MiniPlayer(
                    playback = state.playback,
                    onOpen = { navController.navigate("nowPlaying") },
                    onToggle = viewModel::togglePlayPause,
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
