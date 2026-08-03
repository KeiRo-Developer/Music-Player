package com.keiro.musicplayer.data

import android.net.Uri

/**
 * A single track pulled from MediaStore.
 * [format] is derived from the file extension so the UI can badge
 * lossless files (FLAC / WAV / ALAC) distinctly from lossy ones.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: Uri,
    val albumArtUri: Uri?,
    val filePath: String,
    val format: AudioFormat
) {
    val isLossless: Boolean get() = format == AudioFormat.FLAC || format == AudioFormat.WAV || format == AudioFormat.ALAC
}

enum class AudioFormat {
    MP3, AAC, FLAC, WAV, ALAC, OGG, UNKNOWN
}
