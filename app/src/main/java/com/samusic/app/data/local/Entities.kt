package com.samusic.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.samusic.app.data.Track

/** Track fields embedded into several tables to avoid extra joins. */
data class TrackColumns(
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String?,
    val audioUrl: String,
    val durationSeconds: Int,
    val year: String?,
    val source: String,
    val isLocal: Boolean,
)

fun Track.toColumns() = TrackColumns(
    trackId = id, title = title, artist = artist, album = album,
    artworkUrl = artworkUrl, audioUrl = audioUrl, durationSeconds = durationSeconds,
    year = year, source = source, isLocal = isLocal,
)

fun TrackColumns.toTrack() = Track(
    id = trackId, title = title, artist = artist, album = album,
    artworkUrl = artworkUrl, audioUrl = audioUrl, durationSeconds = durationSeconds,
    year = year, source = source, isLocal = isLocal,
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    @Embedded val track: TrackColumns,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String,
    val description: String = "",
    val coverUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

// "trackId" in the composite key is provided by the embedded TrackColumns.
@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackEntity(
    val playlistId: Long,
    val position: Int,
    @Embedded val track: TrackColumns,
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    @Embedded val track: TrackColumns,
    val playedAt: Long = System.currentTimeMillis(),
)
