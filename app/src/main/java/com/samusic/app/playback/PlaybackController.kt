package com.samusic.app.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.samusic.app.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Snapshot of playback state exposed to the UI. */
data class PlayerState(
    val current: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
)

/**
 * Bridges the Compose UI to the MediaController backed by [MusicService].
 * Holds the current queue as domain [Track]s so the UI can render metadata
 * without re-reading MediaItems.
 */
class PlaybackController(context: Context) {

    private val appContext = context.applicationContext
    private var controller: MediaController? = null
    private var queue: List<Track> = emptyList()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    var onTrackStarted: ((Track) -> Unit)? = null

    /** Swaps a track's audio URL for a local file when it has been downloaded. */
    var urlResolver: (Track) -> Track = { it }

    fun connect() {
        if (controller != null) return
        val token = SessionToken(appContext, ComponentName(appContext, MusicService::class.java))
        val future: ListenableFuture<MediaController> =
            MediaController.Builder(appContext, token).buildAsync()
        future.addListener({
            controller = future.get()
            controller?.addListener(playerListener)
            syncState()
        }, MoreExecutors.directExecutor())
    }

    fun release() {
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncState()
            val idx = player.currentMediaItemIndex
            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                queue.getOrNull(idx)?.let { onTrackStarted?.invoke(it) }
            }
        }
    }

    private fun syncState() {
        val c = controller ?: return
        val idx = c.currentMediaItemIndex
        _state.value = PlayerState(
            current = queue.getOrNull(idx),
            isPlaying = c.isPlaying,
            positionMs = c.currentPosition.coerceAtLeast(0),
            durationMs = c.duration.coerceAtLeast(0),
            shuffle = c.shuffleModeEnabled,
            repeatMode = c.repeatMode,
        )
    }

    /** Refreshes the position field; call from a UI ticker while playing. */
    fun refreshProgress() = syncState()

    fun playQueue(tracks: List<Track>, startIndex: Int) {
        val c = controller ?: return
        if (tracks.isEmpty()) return
        queue = tracks
        c.setMediaItems(tracks.map { urlResolver(it).toMediaItem() }, startIndex, 0L)
        c.prepare()
        c.play()
        queue.getOrNull(startIndex)?.let { onTrackStarted?.invoke(it) }
    }

    fun playSingle(track: Track) = playQueue(listOf(track), 0)

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() = controller?.seekToNextMediaItem()
    fun previous() = controller?.seekToPreviousMediaItem()
    fun seekTo(ms: Long) = controller?.seekTo(ms)

    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    private fun Track.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(artworkUrl?.let { android.net.Uri.parse(it) })
            .build()
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(audioUrl)
            .setMediaMetadata(metadata)
            .build()
    }
}
