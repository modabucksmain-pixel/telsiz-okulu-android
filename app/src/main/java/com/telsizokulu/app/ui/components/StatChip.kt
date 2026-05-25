package com.telsizokulu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.SpektrumAccent
import com.telsizokulu.app.ui.theme.SpektrumStreak

enum class StatChipKind { STREAK, XP }

// Handoff SVG path data (24×24 viewBox) — emoji yerine vektör ikon
private const val FLAME_PATH = "M12 3c1 3-2 4-2 7 0 2 1 4 3 4-1 0-2 2-1 4-3 0-6-3-6-7 0-3 2-5 3-6 1 1 2 1 3-2z"
private const val STAR_PATH  = "M12 2l2.6 6.4L21 9.3l-5 4.3 1.6 6.6L12 16.8 6.4 20.2 8 13.6 3 9.3l6.4-.9L12 2z"

@Composable
fun StatChip(
    kind: StatChipKind,
    value: Int,
    modifier: Modifier = Modifier,
) {
    val fg = if (kind == StatChipKind.STREAK) SpektrumStreak else SpektrumAccent
    val pathData = if (kind == StatChipKind.STREAK) FLAME_PATH else STAR_PATH
    val iconPath = remember(pathData) { PathParser().parsePathString(pathData).toPath() }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(fg.copy(alpha = 0.094f))
            .border(1.dp, fg.copy(alpha = 0.157f), CircleShape)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(13.dp)) {
            val s = size.minDimension / 24f
            scale(s, s, pivot = Offset.Zero) {
                drawPath(iconPath, fg)
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = value.toString(),
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = fg,
        )
    }
}
