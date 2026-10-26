package com.samusic.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samusic.app.ui.HomeViewModel
import com.samusic.app.ui.components.TrackRow

@Composable
fun HomeScreen(vm: HomeViewModel, contentPadding: PaddingValues) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()

    // Infinite scroll: load next page when near the bottom.
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.tracks.size - 4 && state.tracks.isNotEmpty()
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) vm.loadMore()
    }

    when {
        state.missingKey -> MissingKeyNotice(contentPadding)
        state.tracks.isEmpty() && state.loading -> Centered(contentPadding) { CircularProgressIndicator() }
        state.tracks.isEmpty() && state.error != null ->
            Centered(contentPadding) { Text("Erro: ${state.error}") }
        else -> LazyColumn(state = listState, contentPadding = contentPadding, modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    "Populares agora",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(state.tracks, key = { it.id }) { track ->
                TrackRow(track = track, onClick = { vm.play(track) })
            }
            if (state.loading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun Centered(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { content() }
}

@Composable
fun MissingKeyNotice(padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            "Não foi possível carregar o catálogo agora. Verifique sua conexão e tente novamente.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
