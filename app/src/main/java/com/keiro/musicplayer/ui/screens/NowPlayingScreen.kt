package com.keiro.musicplayer.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.keiro.musicplayer.data.Song
import com.keiro.musicplayer.lyrics.LyricsParser
import com.keiro.musicplayer.lyrics.LyricsResult
import com.keiro.musicplayer.ui.components.FormatBadge
import kotlinx.coroutines.launch

@Composable
fun NowPlayingScreen(
    song: Song,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    lyrics: LyricsResult,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        // Blurred, darkened album art as an ambient backdrop.
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(60.dp)
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.92f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(Modifier.height(24.dp))

            Text(
                song.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    song.artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.width(8.dp))
                FormatBadge(label = song.format.name, isLossless = song.isLossless)
            }

            Spacer(Modifier.height(20.dp))

            LyricsPanel(
                lyrics = lyrics,
                positionMs = positionMs,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            SeekBar(positionMs = positionMs, durationMs = durationMs, onSeek = onSeek)

            Spacer(Modifier.height(12.dp))

            TransportControls(
                isPlaying = isPlaying,
                onTogglePlay = onTogglePlay,
                onNext = onNext,
                onPrevious = onPrevious
            )
        }
    }
}

@Composable
private fun LyricsPanel(lyrics: LyricsResult, positionMs: Long, modifier: Modifier = Modifier) {
    when (lyrics) {
        is LyricsResult.Synced -> {
            val listState = rememberLazyListState()
            val activeIndex = LyricsParser.currentLineIndex(lyrics.lines, positionMs)
            val scope = rememberCoroutineScope()

            LaunchedEffect(activeIndex) {
                if (activeIndex >= 0) {
                    scope.launch {
                        listState.animateScrollToItem(
                            index = (activeIndex - 2).coerceAtLeast(0)
                        )
                    }
                }
            }

            LazyColumn(state = listState, modifier = modifier) {
                items(lyrics.lines.size) { i ->
                    val active = i == activeIndex
                    Text(
                        text = lyrics.lines[i].text.ifBlank { "…" },
                        style = if (active) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) Color.White else Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 12.dp)
                    )
                }
            }
        }
        is LyricsResult.Plain -> {
            LazyColumn(modifier = modifier) {
                item {
                    Text(
                        text = lyrics.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    )
                }
            }
        }
        LyricsResult.None -> {
            Box(modifier, contentAlignment = Alignment.Center) {
                Text(
                    "No lyrics found for this track.\nAdd a matching .lrc file next to the song.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SeekBar(positionMs: Long, durationMs: Long, onSeek: (Long) -> Unit) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val progress = if (durationMs > 0) (dragValue ?: positionMs.toFloat()) / durationMs else 0f

    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = { dragValue = it * durationMs },
            onValueChangeFinished = {
                dragValue?.let { onSeek(it.toLong()) }
                dragValue = null
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
            )
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMs(dragValue?.toLong() ?: positionMs), color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
            Text(formatMs(durationMs), color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TransportControls(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val playScale by animateDpAsState(if (isPlaying) 72.dp else 68.dp, label = "playScale")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Icon(
            Icons.Filled.SkipPrevious,
            contentDescription = "Previous",
            tint = Color.White,
            modifier = Modifier.size(36.dp).clickableIcon(onPrevious)
        )
        Box(
            modifier = Modifier
                .size(playScale)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFB388FF), Color(0xFF64FFDA))))
                .clickableIcon(onTogglePlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
        Icon(
            Icons.Filled.SkipNext,
            contentDescription = "Next",
            tint = Color.White,
            modifier = Modifier.size(36.dp).clickableIcon(onNext)
        )
    }
}

private fun Modifier.clickableIcon(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
