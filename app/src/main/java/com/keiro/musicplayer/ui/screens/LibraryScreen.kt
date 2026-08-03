package com.keiro.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.keiro.musicplayer.data.Song
import com.keiro.musicplayer.ui.components.FormatBadge
import com.keiro.musicplayer.ui.theme.AccentViolet
import com.keiro.musicplayer.ui.theme.SurfaceElevated

/**
 * @param onSongClick receives the list currently displayed (post-search-filter)
 * along with the tapped index into *that* list, so the queue that starts playing
 * always matches what's on screen — searching for something and tapping it plays
 * just the matching results, not the whole unfiltered library.
 */
@Composable
fun LibraryScreen(
    songs: List<Song>,
    onSongClick: (List<Song>, Int) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    val filtered = remember(songs, query) {
        if (query.isBlank()) {
            songs
        } else {
            val q = query.trim()
            songs.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.artist.contains(q, ignoreCase = true) ||
                    it.album.contains(q, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceElevated, MaterialTheme.colorScheme.background)
                )
            )
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                placeholder = { Text("Search title, artist, album") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp)
            )

            when {
                songs.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No tracks found on this device yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                filtered.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No results for \u201c$query\u201d",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                        items(filtered, key = { it.id }) { song ->
                            val index = filtered.indexOf(song)
                            SongRow(song = song, onClick = { onSongClick(filtered, index) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val notePainter = rememberVectorPainter(Icons.Filled.MusicNote)
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = notePainter,
            error = notePainter,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceElevated)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                FormatBadge(label = song.format.name, isLossless = song.isLossless)
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = AccentViolet,
            modifier = Modifier.size(22.dp)
        )
    }
}
