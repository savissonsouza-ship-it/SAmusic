package com.samusic.app.data.repo

import com.samusic.app.BuildConfig
import com.samusic.app.data.Track
import com.samusic.app.data.local.AppDatabase
import com.samusic.app.data.local.FavoriteEntity
import com.samusic.app.data.local.HistoryEntity
import com.samusic.app.data.local.PlaylistEntity
import com.samusic.app.data.local.PlaylistTrackEntity
import com.samusic.app.data.local.toColumns
import com.samusic.app.data.local.toTrack
import com.samusic.app.data.remote.JamendoApi
import com.samusic.app.data.toTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface Outcome<out T> {
    data class Success<T>(val data: T) : Outcome<T>
    data class Error(val message: String) : Outcome<Nothing>
}

const val PAGE_SIZE = 20

class MusicRepository(
    private val api: JamendoApi,
    private val db: AppDatabase,
) {
    private val clientId = BuildConfig.JAMENDO_CLIENT_ID
    val hasApiKey: Boolean get() = clientId.isNotBlank()

    private inline fun <T> guarded(block: () -> T): Outcome<T> =
        if (!hasApiKey) Outcome.Error("MISSING_KEY")
        else try {
            Outcome.Success(block())
        } catch (e: Exception) {
            Outcome.Error(e.message ?: "Falha de rede")
        }

    suspend fun popular(page: Int): Outcome<List<Track>> = guarded {
        api.tracks(clientId, limit = PAGE_SIZE, offset = page * PAGE_SIZE, order = "popularity_total")
            .results.mapNotNull { it.toTrack() }
    }

    suspend fun newest(page: Int): Outcome<List<Track>> = guarded {
        api.tracks(clientId, limit = PAGE_SIZE, offset = page * PAGE_SIZE, order = "releasedate_desc")
            .results.mapNotNull { it.toTrack() }
    }

    suspend fun search(query: String, page: Int): Outcome<List<Track>> = guarded {
        api.search(clientId, query = query, limit = PAGE_SIZE, offset = page * PAGE_SIZE)
            .results.mapNotNull { it.toTrack() }
    }

    suspend fun byGenre(tag: String, page: Int): Outcome<List<Track>> = guarded {
        api.byTag(clientId, tag = tag, limit = PAGE_SIZE, offset = page * PAGE_SIZE)
            .results.mapNotNull { it.toTrack() }
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
