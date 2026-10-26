package com.samusic.app.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.samusic.app.ui.SearchViewModel
import com.samusic.app.ui.components.TrackRow
import com.samusic.app.ui.home.Centered
import com.samusic.app.ui.home.MissingKeyNotice

// Audius genre names (as the API expects them).
private val genres = listOf(
    "Electronic", "Hip-Hop/Rap", "Pop", "Rock", "Metal", "R&B/Soul", "Jazz",
    "Blues", "Classical", "Reggae", "Ambient", "World", "Soundtrack", "Country"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(vm: SearchViewModel, contentPadding: PaddingValues) {
    val state by vm.state.collectAsState()
    var text by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(contentPadding)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; vm.onQueryChange(it) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Buscar músicas, artistas…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { vm.submit() }),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
        )

        if (state.tracks.isEmpty() && !state.loading && state.error == null && !state.missingKey) {
            Text("Explorar gêneros", style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            FlowRow(
                Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genres.forEach { g ->
                    AssistChip(onClick = { text = g; vm.searchGenre(g) }, label = { Text(g) })
                }
            }
        }

        when {
            state.missingKey -> MissingKeyNotice(PaddingValues(0.dp))
            state.loading -> Centered(PaddingValues(0.dp)) { CircularProgressIndicator() }
            state.error != null -> Centered(PaddingValues(0.dp)) { Text("Erro: ${state.error}") }
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(state.tracks, key = { it.id }) { track ->
                    TrackRow(track = track, onClick = { vm.play(track) })
                }
            }
        }
    }
}
