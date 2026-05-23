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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
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
import com.echo.musicplayer.domain.model.Song
import com.echo.musicplayer.ui.app.EchoAppUiState
import com.echo.musicplayer.ui.components.Artwork
import com.echo.musicplayer.ui.components.ScreenHeader
import com.echo.musicplayer.ui.components.SectionCard
import com.echo.musicplayer.ui.components.SongRow
import com.echo.musicplayer.ui.components.SongTrailingMode

@Composable
fun LibraryScreen(
    state: EchoAppUiState,
    contentPadding: PaddingValues,
    onSearch: () -> Unit,
    onUpload: () -> Unit,
    onSongClick: (Song) -> Unit,
    onMore: (Song) -> Unit,
    onNowPlaying: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
    MusicListScreen(
        title = "Library",
        songs = state.songs,
        contentPadding = contentPadding,
        actions = {
            IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = null) }
            IconButton(onClick = onUpload) { Icon(Icons.Filled.Add, contentDescription = null) }
            IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, contentDescription = null) }
        },
        header = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Shuffle Play", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        },
        onSongClick = onSongClick,
        onMore = onMore,
    )
}

@Composable
fun DownloadsScreen(
    state: EchoAppUiState,
    contentPadding: PaddingValues,
    onSearch: () -> Unit,
    onDownload: (Song) -> Unit,
    onDownloadAll: () -> Unit,
    onNowPlaying: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
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
                    IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, contentDescription = null) }
                },
            )
            SectionCard(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Storage Used", style = MaterialTheme.typography.labelMedium)
                        Text("12.45 GB / 50 GB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { 0.25f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("All", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Text("Downloaded", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Text("Downloading", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
        }
        items(state.songs, key = { it.id }) { song ->
            SongRow(
                song = song,
                onClick = { onDownload(song) },
                trailingMode = SongTrailingMode.Download,
            )
        }
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
                text = "${state.downloaded.size} songs downloaded",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
            )
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
    onNowPlaying: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
    MusicListScreen(
        title = "Favorites",
        songs = state.favorites,
        contentPadding = contentPadding,
        actions = {
            IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = null) }
            IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, contentDescription = null) }
        },
        header = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Shuffle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Shuffle Play", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        },
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
    onWifi: (Boolean) -> Unit,
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
            SettingSwitch("Download over Wi-Fi only", state.settings.downloadOverWifiOnly, onWifi)
            SettingSwitch("Keep screen on while playing", state.settings.keepScreenOnWhilePlaying, onKeepScreen)
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Primary color", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                            .clickable { onColor(color) },
                    )
                }
            }
            Text("Storage", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium)
            SettingRow("Storage used", "12.45 GB", Icons.Filled.Storage, onStorage)
            SettingRow("Clear downloaded songs", "", Icons.Filled.Delete) {}
            SettingRow("Clear cache", "256 MB", Icons.Filled.Clear) {}
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
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
            items(state.songs, key = { it.id }) { song ->
                SongRow(song = song, onClick = { onSongClick(song) })
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
    val song = state.playback.currentSong ?: state.songs.firstOrNull()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(
            title = "Now Playing",
            leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
            actions = { IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, contentDescription = null) } },
        )
        if (song != null) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Artwork(song = song, modifier = Modifier.fillMaxWidth().height(300.dp))
                Spacer(Modifier.height(24.dp))
                Text(song.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                    IconButton(onClick = {}) { Icon(Icons.Filled.Shuffle, contentDescription = null) }
                    IconButton(onClick = onPrevious) { Icon(Icons.Filled.SkipPrevious, contentDescription = null, modifier = Modifier.size(30.dp)) }
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(if (state.playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onNext) { Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(30.dp)) }
                    IconButton(onClick = {}) { Icon(Icons.Filled.Menu, contentDescription = null) }
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    NowPlayingAction(if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "Favorite", onFavorite)
                    NowPlayingAction(Icons.Filled.Download, "Download", onDownload)
                    NowPlayingAction(Icons.Filled.QueueMusic, "Queue", onQueue)
                }
            }
        }
    }
}

@Composable
fun UploadScreen(
    state: EchoAppUiState,
    onBack: () -> Unit,
    onUpload: () -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        item {
            ScreenHeader(
                title = "Upload Song",
                leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
            )
            SectionCard(Modifier.padding(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Tap to select MP3 file", fontWeight = FontWeight.SemiBold)
                    Text("or drag and drop here", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            SectionCard(Modifier.padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetadataLine("File name", "Believer.mp3")
                    MetadataLine("Size", "5.00 MB")
                    MetadataLine("Duration", "3:24")
                    MetadataLine("Title", "Believer")
                    MetadataLine("Artist", "Imagine Dragons")
                    MetadataLine("Album", "Evolve")
                }
            }
            Button(
                onClick = onUpload,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(if (state.uploadProgress in 0.01f..0.99f) "Uploading ${(state.uploadProgress * 100).toInt()}%" else "Upload")
            }
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
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        item {
            ScreenHeader(
                title = "Queue",
                leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
                actions = { TextButton(onClick = onClear) { Text("Clear") } },
            )
            Text("Now Playing", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            current?.let { SongRow(song = it, onClick = { onPlay(it) }, trailingMode = SongTrailingMode.Download) }
            Text("Up Next", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        }
        items(upcoming, key = { it.id }) { song ->
            SongRow(song = song, onClick = { onPlay(song) }, onMoreClick = { onRemove(song) })
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
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp), verticalArrangement = Arrangement.Center) {
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
                            Text(song.title, fontWeight = FontWeight.Bold)
                            Text(song.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formatDuration(song.durationMs), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                OptionRow(Icons.Filled.PlaylistAdd, "Play Next", onPlayNext)
                OptionRow(Icons.Filled.Add, "Add to Queue", onPlayNext)
                OptionRow(Icons.Filled.Favorite, "Add to Favorites", onFavorite)
                OptionRow(Icons.Filled.Download, "Download", onDownload)
                OptionRow(Icons.Filled.Edit, "Edit Song Info", onClick = {})
                OptionRow(Icons.Filled.Share, "Share", onClick = {})
                OptionRow(Icons.Filled.Delete, "Delete from Library", onDeleteDownload, isDanger = true)
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
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                    Text("${(state.downloadAllProgress * 100).toInt()}%", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("23.6 MB / 32.6 MB", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
            Text(
                text = "Downloading ${state.downloaded.size} of ${state.songs.size} songs",
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        items(state.songs.take(4), key = { it.id }) { song ->
            SongRow(song = song, onClick = {}, trailingMode = SongTrailingMode.Download)
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
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        item {
            ScreenHeader(
                title = "Storage & Data",
                leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
            )
            SectionCard(Modifier.padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Storage Used", fontWeight = FontWeight.SemiBold)
                        Text("12.45 GB / 50 GB", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { 0.25f }, modifier = Modifier.fillMaxWidth())
                    StorageLegend("Downloaded Songs", formatBytes(state.storageUsage.downloadedBytes), MaterialTheme.colorScheme.primary)
                    StorageLegend("Cache", formatBytes(state.storageUsage.cacheBytes), Color(0xFFE8C766))
                    StorageLegend("Other Data", formatBytes(state.storageUsage.otherBytes), Color(0xFF5AA9FF))
                }
            }
            SectionCard(Modifier.padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    MetadataLine("Total songs", "${state.songs.size}")
                    MetadataLine("Total size", "10.12 GB")
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
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ScreenHeader(
            title = "About",
            leading = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) } },
        )
        Column(Modifier.fillMaxWidth().padding(top = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(88.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text("Offline Music Player", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SectionCard(Modifier.padding(16.dp)) {
            Column {
                OptionRow(Icons.Filled.Info, "What's new", onClick = {})
                OptionRow(Icons.Filled.Info, "Privacy Policy", onClick = {})
                OptionRow(Icons.Filled.Info, "Terms of Use", onClick = {})
                OptionRow(Icons.Filled.Info, "Open Source Licenses", onClick = {})
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

@Composable
private fun MusicListScreen(
    title: String,
    songs: List<Song>,
    contentPadding: PaddingValues,
    actions: @Composable () -> Unit,
    header: @Composable () -> Unit,
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
            header()
        }
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

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChecked)
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
        Text(value, style = MaterialTheme.typography.bodyMedium)
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
