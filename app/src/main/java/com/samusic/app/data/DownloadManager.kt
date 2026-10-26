package com.samusic.app.data

import android.content.Context
import android.net.Uri
import com.samusic.app.data.local.AppDatabase
import com.samusic.app.data.local.DownloadEntity
import com.samusic.app.data.local.toColumns
import com.samusic.app.data.local.toTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * Offline downloads. Audio is fetched from the track's download/stream URL and
 * stored in the app's private files dir; the DB keeps the metadata so the
 * offline library survives restarts. Playback prefers the local file when a
 * track is downloaded.
 */
class DownloadManager(
    context: Context,
    private val db: AppDatabase,
    private val client: OkHttpClient,
    scope: CoroutineScope,
) {
    private val appContext = context.applicationContext
    private val dir = File(appContext.filesDir, "downloads").apply { mkdirs() }

    private val _ids = MutableStateFlow<Set<String>>(emptySet())
    val downloadedIds: StateFlow<Set<String>> = _ids.asStateFlow()

    init {
        db.downloadDao().observeIds()
            .onEach { ids -> _ids.value = ids.toSet() }
            .launchIn(scope)
    }

    fun downloaded(): Flow<List<Track>> =
        db.downloadDao().observeAll().map { list -> list.map { it.track.toTrack().copy(isLocal = true) } }

    fun isDownloaded(id: String): Flow<Boolean> = db.downloadDao().isDownloaded(id)

    private fun fileFor(id: String) = File(dir, "$id.mp3")

    /** Local file URI if this track is downloaded and present on disk, else null. */
    fun localUriOrNull(id: String): String? {
        if (!_ids.value.contains(id)) return null
        val f = fileFor(id)
        return if (f.exists()) Uri.fromFile(f).toString() else null
    }

    suspend fun download(track: Track): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = track.downloadUrl ?: track.audioUrl
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext Result.failure(Exception("HTTP ${resp.code}"))
                val body = resp.body ?: return@withContext Result.failure(Exception("Resposta vazia"))
                fileFor(track.id).outputStream().use { out -> body.byteStream().copyTo(out) }
            }
            db.downloadDao().add(
                DownloadEntity(
                    id = track.id,
                    track = track.toColumns(),
                    filePath = fileFor(track.id).absolutePath,
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            fileFor(track.id).delete()
            Result.failure(e)
        }
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        fileFor(id).delete()
        db.downloadDao().remove(id)
    }
}
