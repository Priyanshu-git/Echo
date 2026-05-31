package com.echo.musicplayer.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.echo.musicplayer.core.util.formatBytes
import com.echo.musicplayer.core.util.formatDuration
import com.echo.musicplayer.domain.model.DownloadStatus
import com.echo.musicplayer.domain.model.LibraryStatus
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.domain.model.ThemeMode
import com.echo.musicplayer.ui.app.EchoAppUiState
import com.echo.musicplayer.ui.components.Artwork
import com.echo.musicplayer.ui.components.EmptyState
import com.echo.musicplayer.ui.components.ScreenHeader
import com.echo.musicplayer.ui.components.SectionCard
import com.echo.musicplayer.ui.components.SongRow
import com.echo.musicplayer.ui.components.SongTrailingMode

@Composable
fun LibraryScreen(
    state: EchoAppUiState,
    contentPadding: PaddingValues,
    onSearch: () -> Unit,
    onSongClick: (Song) -> Unit,
    onMore: (Song) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Library",
                actions = { IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = null) } },
            )
            LibraryStatusBanner(state.libraryStatus, state.songs.isNotEmpty())
        }
        if (state.songs.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.MusicNote,
                    title = when (state.libraryStatus) {
                        LibraryStatus.CheckingFirestore -> "Checking Firestore"
                        LibraryStatus.Empty -> "No songs found in Firestore"
                        LibraryStatus.Failed -> "Could not reach Firestore"
                        else -> "No songs yet"
                    },
                    body = when (state.libraryStatus) {
                        LibraryStatus.CheckingFirestore -> "Your shared library is being synced."
                        LibraryStatus.Empty -> "Add song metadata to Firestore to populate your private library."
                        LibraryStatus.Failed -> "Check your connection or Firestore configuration, then try again."
                        else -> "Add song metadata to Firestore to populate your private library."
                    },
                )
            }
        } else {
            items(state.songs, key = { it.id }) { song ->
                SongRow(
                    song = song,
                    onClick = { onSongClick(song) },
                    onMoreClick = { onMore(song) },
                    trailingMode = SongTrailingMode.Duration,
                )
            }
        }
    }
}

@Composable
fun DownloadsScreen(
    state: EchoAppUiState,
    contentPadding: PaddingValues,
    onSearch: () -> Unit,
    onDownload: (Song) -> Unit,
    onDownloadAll: () -> Unit,
) {
    var selectedFilter by remember { mutableStateOf(DownloadFilter.All) }
    val visibleSongs = when (selectedFilter) {
        DownloadFilter.All -> state.songs
        DownloadFilter.Downloaded -> state.downloaded
        DownloadFilter.Downloading -> state.downloading
        DownloadFilter.Failed -> state.failedDownloads
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Downloads",
                actions = {
                    IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = null) }
                },
            )
            SectionCard(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Storage Used", style = MaterialTheme.typography.labelMedium)
                        Text(formatBytes(state.storageUsage.downloadedBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (state.totalLibraryBytes == 0L) 0f else (state.storageUsage.downloadedBytes.toFloat() / state.totalLibraryBytes).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                DownloadFilter.entries.forEach { filter ->
                    val count = when (filter) {
                        DownloadFilter.All -> state.songs.size
                        DownloadFilter.Downloaded -> state.downloaded.size
                        DownloadFilter.Downloading -> state.downloading.size
                        DownloadFilter.Failed -> state.failedDownloads.size
                    }
                    Text(
                        text = "${filter.label} ($count)",
                        modifier = Modifier.clickable { selectedFilter = filter },
                        color = if (selectedFilter == filter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        if (visibleSongs.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Download,
                    title = "Nothing to download",
                    body = "Songs from your library will appear here with their current download status.",
                )
            }
        } else {
            items(visibleSongs, key = { it.id }) { song ->
                SongRow(
                    song = song,
                    onClick = { onDownload(song) },
                    trailingMode = SongTrailingMode.Download,
                )
            }
        }
        if (state.songs.isNotEmpty()) {
            item {
                Button(
                    onClick = onDownloadAll,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Download All")
                }
                Text(
                    text = "${state.downloaded.size} downloaded, ${state.downloading.size} downloading, ${state.failedDownloads.size} failed",
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    state: EchoAppUiState,
    contentPadding: PaddingValues,
    onSearch: () -> Unit,
    onSongClick: (Song) -> Unit,
    onFavorite: (Song) -> Unit,
) {
    MusicListScreen(
        title = "Favorites",
        songs = state.favorites,
        contentPadding = contentPadding,
        actions = {
            IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = null) }
        },
        emptyTitle = "No favorites yet",
        emptyBody = "Tap the heart on songs you love and they will collect here.",
        emptyIcon = Icons.Filled.FavoriteBorder,
        onSongClick = onSongClick,
        trailingMode = SongTrailingMode.Favorite,
        onFavorite = onFavorite,
    )
}

@Composable
fun SettingsScreen(
    state: EchoAppUiState,
    contentPadding: PaddingValues,
    onColor: (Long) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onKeepScreen: (Boolean) -> Unit,
    onStorage: () -> Unit,
    onAbout: () -> Unit,
) {
    val colors = listOf(0xFFB45CFF, 0xFF4FD1C5, 0xFFFF6B7A, 0xFFE8C766, 0xFF5AA9FF)
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item { ScreenHeader(title = "Settings") }
        item {
            Text("General", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
            ThemeModeRow(state.settings.themeMode, onThemeMode)
            SettingSwitch("Keep screen on while playing", state.settings.keepScreenOnWhilePlaying, onKeepScreen)
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Primary color", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(color.toInt()))
                            .clickable { onColor(color) },
                    )
                }
            }
            Text("Storage", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
            SettingRow("Storage used", formatBytes(state.storageUsage.downloadedBytes), Icons.Filled.Storage, onStorage)
            Text("About", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
            SettingRow("App version", "1.0.0", Icons.Filled.Info, onAbout)
        }
    }
}

@Composable
fun SearchScreen(
    state: EchoAppUiState,
    onBack: () -> Unit,
    onQuery: (String) -> Unit,
    onSongClick: (Song) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onQuery,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Search") },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onQuery("") }) { Icon(Icons.Filled.Clear, contentDescription = null) }
                    }
                },
            )
        }
        LazyColumn {
            if (state.songs.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Search,
                        title = if (state.searchQuery.isBlank()) "Search your library" else "No matching songs",
                        body = if (state.searchQuery.isBlank()) {
                            "Search by title, artist, or album."
                        } else {
                            "Try a different title, artist, or album."
                        },
                    )
                }
            } else {
                items(state.songs, key = { it.id }) { song ->
                    SongRow(song = song, onClick = { onSongClick(song) })
                }
            }
        }
    }
}

@Composable
fun NowPlayingScreen(
    state: EchoAppUiState,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    onQueue: () -> Unit,
) {
    val song = state.playback.currentSong
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        ScreenHeader(
            title = "Now Playing",
            leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
        )
        if (song != null) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Artwork(song = song, modifier = Modifier.fillMaxWidth().height(300.dp))
                Spacer(Modifier.height(24.dp))
                Text(song.title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Slider(
                    value = state.playback.progressMs.toFloat().coerceIn(0f, song.durationMs.toFloat()),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..song.durationMs.toFloat(),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDuration(state.playback.progressMs), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text(formatDuration(song.durationMs), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) { Icon(Icons.Filled.SkipPrevious, contentDescription = null, modifier = Modifier.size(30.dp)) }
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(if (state.playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onNext) { Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(30.dp)) }
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    NowPlayingAction(if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "Favorite", onFavorite)
                    NowPlayingAction(Icons.Filled.Download, "Download", onDownload)
                    NowPlayingAction(Icons.Filled.QueueMusic, "Queue", onQueue)
                }
            }
        } else {
            EmptyState(
                icon = Icons.Filled.MusicNote,
                title = "Nothing playing",
                body = "Pick a song from the library to start listening.",
            )
        }
    }
}

@Composable
fun QueueScreen(
    state: EchoAppUiState,
    onBack: () -> Unit,
    onPlay: (Song) -> Unit,
    onRemove: (Song) -> Unit,
    onClear: () -> Unit,
) {
    val current = state.playback.currentSong
    val upcoming = state.playback.queue.filterNot { it.id == current?.id }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        item {
            ScreenHeader(
                title = "Queue",
                leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
                actions = { TextButton(onClick = onClear) { Text("Clear") } },
            )
            Text("Now Playing", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            if (current == null) {
                EmptyState(
                    icon = Icons.Filled.QueueMusic,
                    title = "Queue is empty",
                    body = "Start playback from the library to build a queue.",
                )
            } else {
                SongRow(song = current, onClick = { onPlay(current) }, trailingMode = SongTrailingMode.Download)
            }
            Text("Up Next", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        }
        items(upcoming, key = { it.id }) { song ->
            SongRow(song = song, onClick = { onPlay(song) }, onMoreClick = { onRemove(song) }, actionIcon = Icons.Filled.Clear)
        }
    }
}

@Composable
fun SongOptionsScreen(
    song: Song?,
    onBack: () -> Unit,
    onPlayNext: () -> Unit,
    onFavorite: () -> Unit,
    onDownload: () -> Unit,
    onDeleteDownload: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        ) {
            Column {
                if (song != null) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Artwork(song, Modifier.size(56.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(song.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatDuration(song.durationMs), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                OptionRow(Icons.Filled.PlayArrow, "Play Now", onPlayNext)
                OptionRow(Icons.Filled.Favorite, if (song?.isFavorite == true) "Remove from Favorites" else "Add to Favorites", onFavorite)
                OptionRow(Icons.Filled.Download, "Download", onDownload)
                if (song?.downloadStatus == DownloadStatus.Downloaded) {
                    OptionRow(Icons.Filled.Delete, "Remove Download", onDeleteDownload, isDanger = true)
                }
                OptionRow(Icons.Filled.Clear, "Cancel", onBack)
            }
        }
    }
}

@Composable
fun DownloadAllScreen(
    state: EchoAppUiState,
    onBack: () -> Unit,
    onCancel: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        item {
            ScreenHeader(
                title = "Download All",
                leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
            )
            Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { state.downloadAllProgress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(156.dp),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(state.downloadAllProgress * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("${formatBytes(state.storageUsage.downloadedBytes)} / ${formatBytes(state.totalLibraryBytes)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
            Text(
                text = if (state.batchDownloadIds.isEmpty()) {
                    "No active batch download"
                } else {
                    "Downloading ${state.batchCompletedCount} of ${state.batchDownloadIds.size} songs; ${state.batchFailedCount} failed"
                },
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        items(state.batchSongs.ifEmpty { state.downloading }.take(8), key = { it.id }) { song ->
            SongRow(song = song, trailingMode = SongTrailingMode.Download)
        }
        item {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun StorageScreen(
    state: EchoAppUiState,
    onBack: () -> Unit,
    onClearDownloads: () -> Unit,
    onClearCache: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        item {
            ScreenHeader(
                title = "Storage & Data",
                leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
            )
            SectionCard(Modifier.padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Storage Used", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text(formatBytes(state.storageUsage.totalBytes), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (state.totalLibraryBytes == 0L) 0f else (state.storageUsage.downloadedBytes.toFloat() / state.totalLibraryBytes).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StorageLegend("Downloaded Songs", formatBytes(state.storageUsage.downloadedBytes), MaterialTheme.colorScheme.primary)
                    StorageLegend("Cache", formatBytes(state.storageUsage.cacheBytes), Color(0xFFE8C766))
                    StorageLegend("Other Data", formatBytes(state.storageUsage.otherBytes), Color(0xFF5AA9FF))
                }
            }
            SectionCard(Modifier.padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    MetadataLine("Total songs", "${state.songs.size}")
                    MetadataLine("Total size", formatBytes(state.totalLibraryBytes))
                    Button(onClick = onClearDownloads, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(8.dp)) {
                        Text("Clear Downloaded Songs")
                    }
                }
            }
            SectionCard(Modifier.padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    MetadataLine("Cache size", formatBytes(state.storageUsage.cacheBytes))
                    Button(onClick = onClearCache, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), shape = RoundedCornerShape(8.dp)) {
                        Text("Clear Cache")
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        ScreenHeader(
            title = "About",
            leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
        )
        Column(Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(88.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text("Offline Music Player", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SectionCard(Modifier.padding(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("Private, offline-capable music playback for a small shared library.", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Text("Open-source licenses and policy screens will be added when external production integrations are finalized.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(
            text = "Made with love for music lovers",
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private enum class DownloadFilter(val label: String) { All("All"), Downloaded("Downloaded"), Downloading("Downloading"), Failed("Failed") }

@Composable
private fun LibraryStatusBanner(status: LibraryStatus, hasCachedSongs: Boolean) {
    val message = when (status) {
        LibraryStatus.CheckingFirestore -> "Checking Firestore..."
        LibraryStatus.Synced -> "Library synced"
        LibraryStatus.Empty -> "No songs found in Firestore"
        LibraryStatus.OfflineUsingCache -> "Could not reach Firestore, showing saved songs"
        LibraryStatus.Failed -> "Could not reach Firestore"
        LibraryStatus.Idle -> null
    }
    if (message != null && (status != LibraryStatus.Synced || hasCachedSongs)) {
        Text(
            text = message,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            color = when (status) {
                LibraryStatus.Failed -> MaterialTheme.colorScheme.error
                LibraryStatus.OfflineUsingCache -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun MusicListScreen(
    title: String,
    songs: List<Song>,
    contentPadding: PaddingValues,
    actions: @Composable () -> Unit,
    emptyTitle: String,
    emptyBody: String,
    emptyIcon: ImageVector,
    onSongClick: (Song) -> Unit,
    onMore: ((Song) -> Unit)? = null,
    trailingMode: SongTrailingMode = SongTrailingMode.Duration,
    onFavorite: ((Song) -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        item {
            ScreenHeader(title = title, actions = actions)
        }
        if (songs.isEmpty()) {
            item {
                EmptyState(icon = emptyIcon, title = emptyTitle, body = emptyBody)
            }
        } else {
            items(songs, key = { it.id }) { song ->
                SongRow(
                    song = song,
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavorite?.invoke(song) },
                    onMoreClick = onMore?.let { { it(song) } },
                    trailingMode = trailingMode,
                )
            }
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun ThemeModeRow(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Theme", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                if (mode == selected) {
                    Button(onClick = { onSelected(mode) }, shape = RoundedCornerShape(8.dp)) {
                        Text(mode.name)
                    }
                } else {
                    OutlinedButton(onClick = { onSelected(mode) }, shape = RoundedCornerShape(8.dp)) {
                        Text(mode.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        if (value.isNotBlank()) Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MetadataLine(label: String, value: String) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun OptionRow(icon: ImageVector, label: String, onClick: () -> Unit, isDanger: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
}

@Composable
private fun NowPlayingAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(6.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun StorageLegend(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp))
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}
