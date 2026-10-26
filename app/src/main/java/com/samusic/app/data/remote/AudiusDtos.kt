package com.samusic.app.data.remote.dto

import com.squareup.moshi.Json

// Parsed reflectively via KotlinJsonAdapterFactory (no codegen needed).

data class AudiusHostsResponse(
    @Json(name = "data") val data: List<String> = emptyList()
)

data class AudiusTracksResponse(
    @Json(name = "data") val data: List<AudiusTrack> = emptyList()
)

data class AudiusTrack(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String?,
    @Json(name = "duration") val duration: Int?,
    @Json(name = "genre") val genre: String?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "user") val user: AudiusUser?,
    @Json(name = "artwork") val artwork: AudiusArtwork?,
)

data class AudiusUser(
    @Json(name = "name") val name: String?
)

data class AudiusArtwork(
    @Json(name = "150x150") val img150: String?,
    @Json(name = "480x480") val img480: String?,
    @Json(name = "1000x1000") val img1000: String?,
)
