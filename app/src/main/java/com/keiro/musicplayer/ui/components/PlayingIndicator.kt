package com.keiro.musicplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.keiro.musicplayer.ui.theme.AccentCyan
import com.keiro.musicplayer.ui.theme.LosslessGold

/** Three little bars that bounce at staggered speeds while a track is playing. */
@Composable
fun EqualizerBars(playing: Boolean, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        val delays = listOf(0, 120, 260)
        delays.forEach { delayMs ->
            val transition = rememberInfiniteTransition(label = "eq")
            val height by transition.animateFloat(
                initialValue = 4f,
                targetValue = if (playing) 16f else 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500 + delayMs, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar"
            )
            Box(height)
        }
    }
}

@Composable
private fun Box(heightDp: Float) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .width(3.dp)
            .height(heightDp.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(AccentCyan)
    )
    androidx.compose.foundation.layout.Spacer(Modifier.width(2.dp))
}

/** Gold pill badge marking a track as lossless (FLAC / WAV / ALAC). */
@Composable
fun FormatBadge(label: String, isLossless: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isLossless) LosslessGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)
    val fg = if (isLossless) LosslessGold else Color.White.copy(alpha = 0.6f)
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
