package com.samusic.app.data

import com.samusic.app.data.remote.dto.TrackDto

/** Clean domain model used across UI and playback. */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUrl: String?,
    val audioUrl: String,
    val durationSeconds: Int,
    val year: String?,
    val source: String = "Jamendo (Creative Commons)",
    val isLocal: Boolean = false,
    // CC download URL when the license permits offline storage (null otherwise).
    val downloadUrl: String? = null,
) {
    val durationLabel: String
        get() {
            val m = durationSeconds / 60
            val s = durationSeconds % 60
            return "%d:%02d".format(m, s)
        }
}

fun TrackDto.toTrack(): Track? {
    val url = audio?.takeIf { it.isNotBlank() } ?: return null
    return Track(
        id = id,
        title = name ?: "Sem título",
        artist = artistName ?: "Artista desconhecido",
        album = albumName ?: "",
        artworkUrl = (albumImage ?: image)?.takeIf { it.isNotBlank() },
        audioUrl = url,
        durationSeconds = duration ?: 0,
        year = releaseDate?.take(4)?.takeIf { it.isNotBlank() },
        downloadUrl = audioDownload?.takeIf { it.isNotBlank() },
    )
}
