package com.samusic.app.data.repo

import com.samusic.app.data.Track
import com.samusic.app.data.local.AppDatabase
import com.samusic.app.data.local.FavoriteEntity
import com.samusic.app.data.local.HistoryEntity
import com.samusic.app.data.local.PlaylistEntity
import com.samusic.app.data.local.PlaylistTrackEntity
import com.samusic.app.data.local.toColumns
import com.samusic.app.data.local.toTrack
import com.samusic.app.data.remote.AudiusApi
import com.samusic.app.data.remote.dto.AudiusTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface Outcome<out T> {
    data class Success<T>(val data: T) : Outcome<T>
    data class Error(val message: String) : Outcome<Nothing>
}

const val PAGE_SIZE = 20

class MusicRepository(
    private val api: AudiusApi,
    private val db: AppDatabase,
) {
    // Audius needs no key; kept for compatibility with existing UI state.
    val hasApiKey: Boolean get() = true

    @Volatile private var cachedHost: String? = null

    private suspend fun host(): String {
        cachedHost?.let { return it }
        val list = api.hosts().data
        val h = list.firstOrNull()?.trimEnd('/')
            ?: throw Exception("Nenhum servidor Audius disponível no momento")
        cachedHost = h
        return h
    }

    private fun streamUrl(host: String, id: String): String =
        "$host/v1/tracks/$id/stream?app_name=${AudiusApi.APP_NAME}"

    private fun AudiusTrack.toTrack(host: String): Track? {
        val tid = id.takeIf { it.isNotBlank() } ?: return null
        val stream = streamUrl(host, tid)
        return Track(
            id = tid,
            title = title ?: "Sem título",
            artist = user?.name ?: "Artista desconhecido",
            album = "",
            artworkUrl = (artwork?.img480 ?: artwork?.img150)?.takeIf { it.isNotBlank() },
            audioUrl = stream,
            durationSeconds = duration ?: 0,
            year = releaseDate?.take(4)?.takeIf { it.isNotBlank() },
            source = "Audius",
            downloadUrl = stream,
        )
    }

    private inline fun <T> guarded(block: () -> T): Outcome<T> =
        try {
            Outcome.Success(block())
        } catch (e: Exception) {
            Outcome.Error(e.message ?: "Falha de rede")
        }

    suspend fun popular(page: Int): Outcome<List<Track>> = guarded {
        val h = host()
        api.trending(url = "$h/v1/tracks/trending", limit = PAGE_SIZE, offset = page * PAGE_SIZE)
            .data.mapNotNull { it.toTrack(h) }
    }

    suspend fun newest(page: Int): Outcome<List<Track>> = popular(page)

    suspend fun search(query: String, page: Int): Outcome<List<Track>> = guarded {
        val h = host()
        // Audius search returns a single batch; paginate client-side.
        val all = api.search(url = "$h/v1/tracks/search", query = query).data.mapNotNull { it.toTrack(h) }
        all.drop(page * PAGE_SIZE).take(PAGE_SIZE)
    }

    suspend fun byGenre(tag: String, page: Int): Outcome<List<Track>> = guarded {
        val h = host()
        api.trending(url = "$h/v1/tracks/trending", limit = PAGE_SIZE, offset = page * PAGE_SIZE, genre = tag)
            .data.mapNotNull { it.toTrack(h) }
    }

    // ---- Favorites ----
    fun favorites(): Flow<List<Track>> =
        db.favoriteDao().observeAll().map { list -> list.map { it.track.toTrack() } }

    fun isFavorite(id: String): Flow<Boolean> = db.favoriteDao().isFavorite(id)

    suspend fun toggleFavorite(track: Track, makeFavorite: Boolean) {
        if (makeFavorite) db.favoriteDao().add(FavoriteEntity(id = track.id, track = track.toColumns()))
        else db.favoriteDao().remove(track.id)
    }

    // ---- History ----
    fun history(): Flow<List<Track>> =
        db.historyDao().observeRecent().map { list -> list.map { it.track.toTrack() } }

    suspend fun recordPlay(track: Track) =
        db.historyDao().record(HistoryEntity(id = track.id, track = track.toColumns()))

    suspend fun clearHistory() = db.historyDao().clear()

    // ---- Playlists ----
    fun playlists(): Flow<List<PlaylistEntity>> = db.playlistDao().observePlaylists()
    fun playlist(id: Long): Flow<PlaylistEntity?> = db.playlistDao().observePlaylist(id)

    fun playlistTracks(id: Long): Flow<List<Track>> =
        db.playlistDao().observeTracks(id).map { list -> list.map { it.track.toTrack() } }

    suspend fun createPlaylist(name: String, description: String): Long =
        db.playlistDao().create(PlaylistEntity(name = name, description = description))

    suspend fun renamePlaylist(entity: PlaylistEntity) = db.playlistDao().update(entity)

    suspend fun deletePlaylist(id: Long) = db.playlistDao().deleteFully(id)

    suspend fun addToPlaylist(playlistId: Long, track: Track) {
        val pos = db.playlistDao().nextPosition(playlistId)
        db.playlistDao().addTrack(
            PlaylistTrackEntity(playlistId = playlistId, position = pos, track = track.toColumns())
        )
    }

    suspend fun removeFromPlaylist(playlistId: Long, trackId: String) =
        db.playlistDao().removeTrack(playlistId, trackId)
}
