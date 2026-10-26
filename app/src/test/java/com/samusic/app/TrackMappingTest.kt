package com.samusic.app

import com.samusic.app.data.remote.dto.TrackDto
import com.samusic.app.data.toTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrackMappingTest {

    private fun dto(audio: String?) = TrackDto(
        id = "42",
        name = "Midnight",
        duration = 185,
        artistId = "7",
        artistName = "Nova",
        albumName = "Aurora",
        albumImage = "https://img/cover.jpg",
        image = null,
        audio = audio,
        audioDownload = "https://audio/42_dl.mp3",
        releaseDate = "2021-05-10",
    )

    @Test
    fun maps_all_fields_when_audio_present() {
        val t = dto("https://audio/42.mp3").toTrack()!!
        assertEquals("42", t.id)
        assertEquals("Midnight", t.title)
        assertEquals("Nova", t.artist)
        assertEquals("Aurora", t.album)
        assertEquals("https://img/cover.jpg", t.artworkUrl)
        assertEquals("https://audio/42.mp3", t.audioUrl)
        assertEquals(185, t.durationSeconds)
        assertEquals("2021", t.year)
        assertEquals("https://audio/42_dl.mp3", t.downloadUrl)
    }

    @Test
    fun drops_track_without_streamable_audio() {
        assertNull(dto(null).toTrack())
        assertNull(dto("").toTrack())
    }

    @Test
    fun duration_label_formats_minutes_and_seconds() {
        val t = dto("https://audio/42.mp3").toTrack()!!
        assertEquals("3:05", t.durationLabel)
    }
}
