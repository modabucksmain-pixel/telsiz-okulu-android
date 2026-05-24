package com.telsizokulu.app.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.SpektrumAccent
import com.telsizokulu.app.ui.theme.SpektrumStreak

enum class StatChipKind { STREAK, XP }

@Composable
fun StatChip(
    kind: StatChipKind,
    value: Int,
    modifier: Modifier = Modifier,
) {
    val fg = if (kind == StatChipKind.STREAK) SpektrumStreak else SpektrumAccent
    val icon = if (kind == StatChipKind.STREAK) "🔥" else "⭐"

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(fg.copy(alpha = 0.094f))
            .border(1.dp, fg.copy(alpha = 0.157f), CircleShape)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 12.sp)
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
