package com.samusic.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.samusic.app.data.Track
import com.samusic.app.data.local.PlaylistEntity
import com.samusic.app.data.repo.MusicRepository
import com.samusic.app.data.repo.Outcome
import com.samusic.app.playback.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Generic UI state for a paginated list of tracks. */
data class ListUiState(
    val loading: Boolean = false,
    val tracks: List<Track> = emptyList(),
    val error: String? = null,
    val missingKey: Boolean = false,
    val endReached: Boolean = false,
    val page: Int = 0,
)

class HomeViewModel(
    private val repo: MusicRepository,
    val playback: PlaybackController,
) : ViewModel() {

    private val _state = MutableStateFlow(ListUiState(loading = true))
    val state: StateFlow<ListUiState> = _state.asStateFlow()

    init { loadMore(reset = true) }

    fun loadMore(reset: Boolean = false) {
        val cur = _state.value
        if (cur.loading && !reset) return
        if (cur.endReached && !reset) return
        val page = if (reset) 0 else cur.page
        _state.value = cur.copy(loading = true, error = null)
        viewModelScope.launch {
            when (val r = repo.popular(page)) {
                is Outcome.Success -> {
                    val merged = if (reset) r.data else cur.tracks + r.data
                    _state.value = ListUiState(
                        loading = false,
                        tracks = merged,
                        page = page + 1,
                        endReached = r.data.isEmpty(),
                    )
                }
                is Outcome.Error -> _state.value = cur.copy(
                    loading = false,
                    error = if (r.message == "MISSING_KEY") null else r.message,
                    missingKey = r.message == "MISSING_KEY",
                )
            }
        }
    }

    fun play(track: Track) {
        val list = _state.value.tracks
        val idx = list.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playback.playQueue(list, idx)
        viewModelScope.launch { repo.recordPlay(track) }
    }
}

class SearchViewModel(
    private val repo: MusicRepository,
    val playback: PlaybackController,
) : ViewModel() {

    private val _state = MutableStateFlow(ListUiState())
    val state: StateFlow<ListUiState> = _state.asStateFlow()

    var query: String = ""
        private set

    fun onQueryChange(q: String) { query = q }

    fun submit() {
        val q = query.trim()
        if (q.isEmpty()) { _state.value = ListUiState(); return }
        _state.value = ListUiState(loading = true)
        viewModelScope.launch {
            when (val r = repo.search(q, 0)) {
                is Outcome.Success -> _state.value = ListUiState(tracks = r.data, page = 1, endReached = r.data.isEmpty())
                is Outcome.Error -> _state.value = ListUiState(
                    error = if (r.message == "MISSING_KEY") null else r.message,
                    missingKey = r.message == "MISSING_KEY",
                )
            }
        }
    }

    fun searchGenre(tag: String) {
        query = tag
        _state.value = ListUiState(loading = true)
        viewModelScope.launch {
            when (val r = repo.byGenre(tag, 0)) {
                is Outcome.Success -> _state.value = ListUiState(tracks = r.data, page = 1, endReached = r.data.isEmpty())
                is Outcome.Error -> _state.value = ListUiState(
                    error = if (r.message == "MISSING_KEY") null else r.message,
                    missingKey = r.message == "MISSING_KEY",
                )
            }
        }
    }

    fun play(track: Track) {
        val list = _state.value.tracks
        val idx = list.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playback.playQueue(list, idx)
        viewModelScope.launch { repo.recordPlay(track) }
    }
}

class LibraryViewModel(
    private val repo: MusicRepository,
    val playback: PlaybackController,
) : ViewModel() {

    val favorites: StateFlow<List<Track>> =
        repo.favorites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<Track>> =
        repo.history().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> =
        repo.playlists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.createPlaylist(name.trim(), "") }
    }

    fun deletePlaylist(id: Long) = viewModelScope.launch { repo.deletePlaylist(id) }

    fun play(tracks: List<Track>, start: Track) {
        val idx = tracks.indexOfFirst { it.id == start.id }.coerceAtLeast(0)
        playback.playQueue(tracks, idx)
        viewModelScope.launch { repo.recordPlay(start) }
    }
}

/** Builds any of the above ViewModels with shared dependencies. */
class SAmusicVMFactory(
    private val repo: MusicRepository,
    private val playback: PlaybackController,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repo, playback) as T
        modelClass.isAssignableFrom(SearchViewModel::class.java) -> SearchViewModel(repo, playback) as T
        modelClass.isAssignableFrom(LibraryViewModel::class.java) -> LibraryViewModel(repo, playback) as T
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
