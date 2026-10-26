package com.samusic.app.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samusic.app.ui.LibraryViewModel
import com.samusic.app.ui.components.TrackRow

private enum class Tab(val label: String) { FAVORITES("Favoritos"), PLAYLISTS("Playlists"), HISTORY("Histórico") }

@Composable
fun LibraryScreen(vm: LibraryViewModel, contentPadding: PaddingValues) {
    var tab by remember { mutableStateOf(Tab.FAVORITES) }
    val favorites by vm.favorites.collectAsState()
    val history by vm.history.collectAsState()
    val playlists by vm.playlists.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Tab.entries.forEach { t ->
                FilterChip(selected = tab == t, onClick = { tab = t }, label = { Text(t.label) })
            }
        }

        when (tab) {
            Tab.FAVORITES -> TrackList(favorites, "Nenhuma música favoritada ainda.") { vm.play(favorites, it) }
            Tab.HISTORY -> TrackList(history, "Seu histórico aparecerá aqui.") { vm.play(history, it) }
            Tab.PLAYLISTS -> Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Suas playlists", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showCreate = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Criar playlist")
                    }
                }
                if (playlists.isEmpty()) {
                    EmptyState("Crie sua primeira playlist com o botão +.")
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(playlists, key = { it.playlistId }) { p ->
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(p.name, style = MaterialTheme.typography.bodyLarge)
                                    if (p.description.isNotBlank())
                                        Text(p.description, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { vm.deletePlaylist(p.playlistId) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Nova playlist") },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true,
                    placeholder = { Text("Nome da playlist") })
            },
            confirmButton = {
                TextButton(onClick = { vm.createPlaylist(name); showCreate = false }) { Text("Criar") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TrackList(tracks: List<com.samusic.app.data.Track>, emptyMsg: String, onPlay: (com.samusic.app.data.Track) -> Unit) {
    if (tracks.isEmpty()) { EmptyState(emptyMsg); return }
    LazyColumn(Modifier.fillMaxSize()) {
        items(tracks, key = { it.id }) { TrackRow(track = it, onClick = { onPlay(it) }) }
    }
}

@Composable
private fun EmptyState(msg: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(msg, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
