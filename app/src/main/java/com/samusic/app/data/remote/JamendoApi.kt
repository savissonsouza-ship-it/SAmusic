package com.samusic.app.data.remote

import com.samusic.app.data.remote.dto.JamendoResponse
import com.samusic.app.data.remote.dto.TrackDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Jamendo API v3.0 — Creative Commons catalog with real, streamable audio.
 * Docs: https://developer.jamendo.com/v3.0
 * A free client_id is required (see README).
 */
interface JamendoApi {

    @GET("tracks/")
    suspend fun tracks(
        @Query("client_id") clientId: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("order") order: String = "popularity_total",
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("imagesize") imageSize: Int = 400,
    ): JamendoResponse<TrackDto>

    @GET("tracks/")
    suspend fun search(
        @Query("client_id") clientId: String,
        @Query("namesearch") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("imagesize") imageSize: Int = 400,
    ): JamendoResponse<TrackDto>

    @GET("tracks/")
    suspend fun byTag(
        @Query("client_id") clientId: String,
        @Query("tags") tag: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("order") order: String = "popularity_total",
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("imagesize") imageSize: Int = 400,
    ): JamendoResponse<TrackDto>

    companion object {
        const val BASE_URL = "https://api.jamendo.com/v3.0/"
    }
}
