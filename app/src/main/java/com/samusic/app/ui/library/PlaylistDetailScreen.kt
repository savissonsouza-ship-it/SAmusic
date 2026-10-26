package com.samusic.app.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samusic.app.data.Track
import com.samusic.app.data.repo.MusicRepository
import com.samusic.app.playback.PlaybackController
import com.samusic.app.ui.components.TrackRow
import kotlinx.coroutines.launch

@Composable
fun PlaylistDetailScreen(
    repository: MusicRepository,
    playback: PlaybackController,
    playlistId: Long,
    onBack: () -> Unit,
) {
    val tracksFlow = remember(playlistId) { repository.playlistTracks(playlistId) }
    val nameFlow = remember(playlistId) { repository.playlist(playlistId) }
    val tracks by tracksFlow.collectAsState(initial = emptyList())
    val playlist by nameFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    fun play(start: Track) {
        val idx = tracks.indexOfFirst { it.id == start.id }.coerceAtLeast(0)
        playback.playQueue(tracks, idx)
        scope.launch { repository.recordPlay(start) }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text(
                playlist?.name ?: "Playlist",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            if (tracks.isNotEmpty()) {
                IconButton(onClick = { play(tracks.first()) }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Tocar tudo",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (tracks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Playlist vazia. Toque uma música e use \"Adicionar à playlist\" no player.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(tracks, key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        onClick = { play(track) },
                        trailing = {
                            IconButton(onClick = {
                                scope.launch { repository.removeFromPlaylist(playlistId, track.id) }
                            }) {
                                Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Remover")
                            }
                        }
                    )
                }
            }
        }
    }
}
