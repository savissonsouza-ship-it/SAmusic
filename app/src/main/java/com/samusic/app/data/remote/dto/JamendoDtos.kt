package com.samusic.app.data.remote.dto

import com.squareup.moshi.Json

// Parsed reflectively via KotlinJsonAdapterFactory (no codegen needed).
data class JamendoResponse<T>(
    @Json(name = "headers") val headers: JamendoHeaders?,
    @Json(name = "results") val results: List<T> = emptyList()
)

data class JamendoHeaders(
    @Json(name = "status") val status: String?,
    @Json(name = "code") val code: Int?,
    @Json(name = "error_message") val errorMessage: String?,
    @Json(name = "results_count") val resultsCount: Int?
)

data class TrackDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String?,
    @Json(name = "duration") val duration: Int?,
    @Json(name = "artist_id") val artistId: String?,
    @Json(name = "artist_name") val artistName: String?,
    @Json(name = "album_name") val albumName: String?,
    @Json(name = "album_image") val albumImage: String?,
    @Json(name = "image") val image: String?,
    @Json(name = "audio") val audio: String?,
    @Json(name = "audiodownload") val audioDownload: String?,
    @Json(name = "releasedate") val releaseDate: String?
)
