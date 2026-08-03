package com.keiro.musicplayer.lyrics

import java.io.File

data class LyricLine(val timeMs: Long, val text: String)

sealed class LyricsResult {
    data class Synced(val lines: List<LyricLine>) : LyricsResult()
    data class Plain(val text: String) : LyricsResult()
    object None : LyricsResult()
}

/**
 * Looks for a `.lrc` file with the same base name as the audio file
 * (standard convention: "Song Title.flac" -> "Song Title.lrc"), and falls
 * back to a `.txt` with plain unsynced lyrics if no LRC is present.
 */
object LyricsParser {

    private val timeTagRegex = Regex("""\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?]""")

    fun loadFor(audioFilePath: String): LyricsResult {
        val base = audioFilePath.substringBeforeLast('.')
        val lrcFile = File("$base.lrc")
        if (lrcFile.exists()) {
            val lines = parseLrc(lrcFile.readText())
            if (lines.isNotEmpty()) return LyricsResult.Synced(lines)
        }
        val txtFile = File("$base.txt")
        if (txtFile.exists()) {
            val text = txtFile.readText().trim()
            if (text.isNotEmpty()) return LyricsResult.Plain(text)
        }
        return LyricsResult.None
    }

    fun parseLrc(raw: String): List<LyricLine> {
        val result = mutableListOf<LyricLine>()
        raw.lineSequence().forEach { line ->
            val matches = timeTagRegex.findAll(line)
            if (matches.none()) return@forEach
            val text = line.substringAfterLast(']').trim()
            matches.forEach { m ->
                val min = m.groupValues[1].toLong()
                val sec = m.groupValues[2].toLong()
                val fracStr = m.groupValues[3]
                val fracMs = when (fracStr.length) {
                    2 -> fracStr.toLong() * 10
                    3 -> fracStr.toLong()
                    else -> 0L
                }
                val timeMs = (min * 60_000) + (sec * 1000) + fracMs
                result += LyricLine(timeMs, text)
            }
        }
        return result.sortedBy { it.timeMs }
    }

    /** Index of the line that should be highlighted at [positionMs]. -1 if before first line. */
    fun currentLineIndex(lines: List<LyricLine>, positionMs: Long): Int {
        var idx = -1
        for (i in lines.indices) {
            if (lines[i].timeMs <= positionMs) idx = i else break
        }
        return idx
    }
}
