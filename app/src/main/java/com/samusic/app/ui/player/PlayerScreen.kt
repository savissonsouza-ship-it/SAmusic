package com.samusic.app.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.media3.common.Player
import com.samusic.app.playback.PlaybackController
import com.samusic.app.ui.PlayerViewModel
import com.samusic.app.ui.components.Artwork
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    controller: PlaybackController,
    actions: PlayerViewModel,
    onCollapse: () -> Unit,
) {
    val state by controller.state.collectAsState()
    val track = state.current

    // Tick the progress bar while playing.
    LaunchedEffect(state.isPlaying) {
        while (state.isPlaying) {
            controller.refreshProgress()
            delay(500)
        }
    }

    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCollapse) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Minimizar")
            }
            Text(
                "Tocando agora",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.size(48.dp))
        }

        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.fillMaxWidth().aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Artwork(track?.artworkUrl, size = 320)
        }

        Spacer(Modifier.height(32.dp))
        Text(
            track?.title ?: "—",
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            track?.artist ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))
        PlayerActions(actions, enabled = track != null)

        Spacer(Modifier.height(16.dp))
        val dur = state.durationMs.coerceAtLeast(1).toFloat()
        val pos = if (scrubbing) scrubValue else state.positionMs.toFloat()
        Slider(
            value = pos.coerceIn(0f, dur),
            valueRange = 0f..dur,
            onValueChange = { scrubbing = true; scrubValue = it },
            onValueChangeFinished = {
                controller.seekTo(scrubValue.toLong())
                scrubbing = false
            }
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMs(pos.toLong()), style = MaterialTheme.typography.labelSmall)
            Text(formatMs(state.durationMs), style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { controller.toggleShuffle() }) {
                Icon(
                    Icons.Filled.Shuffle, contentDescription = "Aleatório",
                    tint = if (state.shuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { controller.previous() }) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Anterior", modifier = Modifier.size(40.dp))
            }
            IconButton(onClick = { controller.togglePlayPause() }, modifier = Modifier.size(72.dp)) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { controller.next() }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Próxima", modifier = Modifier.size(40.dp))
            }
            IconButton(onClick = { controller.cycleRepeat() }) {
                val (icon, tint) = when (state.repeatMode) {
                    Player.REPEAT_MODE_ONE -> Icons.Filled.RepeatOne to MaterialTheme.colorScheme.primary
                    Player.REPEAT_MODE_ALL -> Icons.Filled.Repeat to MaterialTheme.colorScheme.primary
                    else -> Icons.Filled.Repeat to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Icon(icon, contentDescription = "Repetir", tint = tint)
            }
        }

        Spacer(Modifier.height(16.dp))
        track?.let {
            Text(
                "Fonte: ${it.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val total = (ms / 1000).toInt().coerceAtLeast(0)
    return "%d:%02d".format(total / 60, total % 60)
}

@Composable
private fun PlayerActions(vm: PlayerViewModel, enabled: Boolean) {
    val isFav by vm.isFavorite.collectAsState()
    val isDownloaded by vm.isDownloaded.collectAsState()
    val downloading by vm.downloading.collectAsState()
    val playlists by vm.playlists.collectAsState()
    var showPicker by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { vm.toggleFavorite() }, enabled = enabled) {
            Icon(
                if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favoritar",
                tint = if (isFav) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { if (!isDownloaded) vm.download() }, enabled = enabled && !downloading) {
            when {
                downloading -> CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                isDownloaded -> Icon(Icons.Filled.DownloadDone, contentDescription = "Baixado",
                    tint = MaterialTheme.colorScheme.primary)
                else -> Icon(Icons.Filled.Download, contentDescription = "Baixar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = { showPicker = true }, enabled = enabled) {
            Icon(Icons.Filled.PlaylistAdd, contentDescription = "Adicionar à playlist",
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Adicionar à playlist") },
            text = {
                if (playlists.isEmpty()) {
                    Text("Você ainda não tem playlists. Crie uma na aba Biblioteca.")
                } else {
                    Column {
                        playlists.forEach { p ->
                            TextButton(onClick = { vm.addToPlaylist(p.playlistId); showPicker = false }) {
                                Text(p.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) { Text("Fechar") }
            }
        )
    }
}
