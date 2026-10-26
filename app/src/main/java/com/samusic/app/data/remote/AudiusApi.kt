package com.samusic.app.data.remote

import com.samusic.app.data.remote.dto.AudiusHostsResponse
import com.samusic.app.data.remote.dto.AudiusTracksResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Audius API — open, keyless music streaming (artists upload their own music).
 * Docs: https://docs.audius.org/api/
 *
 * Flow: first resolve a discovery host from https://api.audius.co, then call
 * that host's /v1 endpoints via absolute @Url. No API key or signup required.
 */
interface AudiusApi {

    @GET
    suspend fun hosts(@Url url: String = HOSTS_URL): AudiusHostsResponse

    @GET
    suspend fun trending(
        @Url url: String,
        @Query("app_name") appName: String = APP_NAME,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("genre") genre: String? = null,
    ): AudiusTracksResponse

    @GET
    suspend fun search(
        @Url url: String,
        @Query("query") query: String,
        @Query("app_name") appName: String = APP_NAME,
    ): AudiusTracksResponse

    companion object {
        const val APP_NAME = "SAmusic"
        const val HOSTS_URL = "https://api.audius.co"
        // Placeholder base; every call uses an absolute @Url.
        const val BASE_URL = "https://api.audius.co/"
    }
}
