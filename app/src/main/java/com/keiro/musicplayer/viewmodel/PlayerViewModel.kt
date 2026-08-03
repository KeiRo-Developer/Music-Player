package com.keiro.musicplayer.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.keiro.musicplayer.data.MusicRepository
import com.keiro.musicplayer.data.Song
import com.keiro.musicplayer.lyrics.LyricsParser
import com.keiro.musicplayer.lyrics.LyricsResult
import com.keiro.musicplayer.player.PlaybackService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(private val appContext: Context) : ViewModel() {

    private var controller: MediaController? = null
    private val repository = MusicRepository(appContext)

    var library by mutableStateOf<List<Song>>(emptyList())
        private set
    var currentSong by mutableStateOf<Song?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var positionMs by mutableStateOf(0L)
        private set
    var durationMs by mutableStateOf(0L)
        private set
    var lyrics by mutableStateOf<LyricsResult>(LyricsResult.None)
        private set

    init {
        connectController()
        viewModelScope.launch {
            library = repository.loadAllSongs()
        }
        // Lightweight position ticker for the seek bar & synced-lyrics highlight.
        viewModelScope.launch {
            while (true) {
                controller?.let {
                    positionMs = it.currentPosition.coerceAtLeast(0)
                    durationMs = it.duration.coerceAtLeast(0)
                    isPlaying = it.isPlaying
                }
                delay(250)
            }
        }
    }

    private fun connectController() {
        val sessionToken = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, sessionToken).buildAsync()
        future.addListener({
            controller = future.get()
            controller?.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val song = library.find { it.id.toString() == mediaItem?.mediaId }
                    currentSong = song
                    loadLyricsFor(song)
                }
            })
        }, MoreExecutors.directExecutor())
    }

    fun playQueue(songs: List<Song>, startIndex: Int) {
        val items = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri)
                        .build()
                )
                .build()
        }
        controller?.setMediaItems(items, startIndex, 0)
        controller?.prepare()
        controller?.play()
        currentSong = songs.getOrNull(startIndex)
        loadLyricsFor(currentSong)
    }

    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun next() = controller?.seekToNextMediaItem()
    fun previous() = controller?.seekToPreviousMediaItem()
    fun seekTo(ms: Long) = controller?.seekTo(ms)

    private fun loadLyricsFor(song: Song?) {
        lyrics = if (song == null) LyricsResult.None else LyricsParser.loadFor(song.filePath)
    }

    override fun onCleared() {
        controller?.release()
        controller = null
        super.onCleared()
    }
}
