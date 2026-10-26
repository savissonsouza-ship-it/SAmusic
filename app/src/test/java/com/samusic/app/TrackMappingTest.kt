package com.samusic.app

import com.samusic.app.data.Track
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackMappingTest {

    private fun track(duration: Int) = Track(
        id = "42",
        title = "Midnight",
        artist = "Nova",
        album = "",
        artworkUrl = "https://img/cover.jpg",
        audioUrl = "https://node/v1/tracks/42/stream?app_name=SAmusic",
        durationSeconds = duration,
        year = "2021",
    )

    @Test
    fun duration_label_formats_minutes_and_seconds() {
        assertEquals("3:05", track(185).durationLabel)
        assertEquals("0:00", track(0).durationLabel)
        assertEquals("1:00", track(60).durationLabel)
        assertEquals("10:09", track(609).durationLabel)
    }

    @Test
    fun defaults_source_to_audius() {
        assertEquals("Audius", track(100).source)
    }
}
