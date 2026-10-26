package com.samusic.app

import android.app.Application
import com.samusic.app.data.DownloadManager
import com.samusic.app.data.local.AppDatabase
import com.samusic.app.data.remote.Network
import com.samusic.app.data.repo.MusicRepository
import com.samusic.app.playback.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Lightweight manual dependency container. Kept simple (no DI framework) so the
 * project compiles with the minimum of annotation processors.
 */
class SAmusicApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val repository: MusicRepository by lazy {
        MusicRepository(Network.createApi(), AppDatabase.get(this))
    }

    val downloads: DownloadManager by lazy {
        DownloadManager(this, AppDatabase.get(this), Network.client, appScope)
    }

    val playback: PlaybackController by lazy { PlaybackController(this) }

    companion object {
        lateinit var instance: SAmusicApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
