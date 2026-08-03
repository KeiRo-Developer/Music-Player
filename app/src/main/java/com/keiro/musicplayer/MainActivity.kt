package com.keiro.musicplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keiro.musicplayer.data.Song
import com.keiro.musicplayer.ui.screens.LibraryScreen
import com.keiro.musicplayer.ui.screens.NowPlayingScreen
import com.keiro.musicplayer.ui.theme.MusicPlayerTheme
import com.keiro.musicplayer.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val audioPermission =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* Compose recomposes via activity restart of scan if needed */ }

        if (checkSelfPermission(audioPermission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermission.launch(audioPermission)
        }

        setContent {
            MusicPlayerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: PlayerViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            @Suppress("UNCHECKED_CAST")
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                return PlayerViewModel(applicationContext) as T
                            }
                        }
                    )

                    var nowPlayingOpen by remember { mutableStateOf(false) }

                    LibraryScreen(
                        songs = viewModel.library,
                        onSongClick = { displayedSongs, index ->
                            viewModel.playQueue(displayedSongs, index)
                            nowPlayingOpen = true
                        }
                    )

                    AnimatedVisibility(
                        visible = nowPlayingOpen && viewModel.currentSong != null,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        val song: Song? = viewModel.currentSong
                        if (song != null) {
                            androidx.activity.compose.BackHandler { nowPlayingOpen = false }
                            NowPlayingScreen(
                                song = song,
                                isPlaying = viewModel.isPlaying,
                                positionMs = viewModel.positionMs,
                                durationMs = viewModel.durationMs,
                                lyrics = viewModel.lyrics,
                                onTogglePlay = viewModel::togglePlayPause,
                                onNext = viewModel::next,
                                onPrevious = viewModel::previous,
                                onSeek = viewModel::seekTo
                            )
                        }
                    }
                }
            }
        }
    }
}
