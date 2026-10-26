package com.samusic.app.data

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
    val source: String = "Audius",
    val isLocal: Boolean = false,
    // URL used to fetch the audio for offline storage (defaults to the stream URL).
    val downloadUrl: String? = null,
) {
    val durationLabel: String
        get() {
            val m = durationSeconds / 60
            val s = durationSeconds % 60
            return "%d:%02d".format(m, s)
        }
}
