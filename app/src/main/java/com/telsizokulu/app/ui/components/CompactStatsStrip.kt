package com.telsizokulu.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.MutedMono
import com.telsizokulu.app.ui.theme.NumeralMono
import com.telsizokulu.app.ui.theme.SpektrumAccent
import com.telsizokulu.app.ui.theme.SpektrumMuted
import com.telsizokulu.app.ui.theme.SpektrumStreak
import com.telsizokulu.app.ui.theme.SpektrumStreakTint
import com.telsizokulu.app.ui.theme.Danger
import com.telsizokulu.app.ui.theme.Success
import com.telsizokulu.app.ui.theme.Warn
import com.telsizokulu.app.ui.theme.neonGlow

@Composable
fun CompactStatsStrip(
    xp: Int,
    seviye: Int,
    seviyePct: Float,
    examDays: Int,
    examReady: Float,
    zayifKonu: Int,
    onSinavClick: () -> Unit,
    onPratikClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val radius = RoundedCornerShape(8.dp)
    val animXp by animateIntAsState(targetValue = xp, animationSpec = tween(800), label = "xp")
    val animPct by animateFloatAsState(
        targetValue = seviyePct.coerceIn(0f, 1f),
        animationSpec = tween(800), label = "pct"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // XP bar
        Row(
            modifier = Modifier
                .weight(1f)
                .neonGlow(SpektrumAccent, cornerRadius = 8.dp, elevation = 8.dp, glowAlpha = 0.3f, borderAlpha = 0.4f)
                .clip(radius)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = seviye.toString(),
                style = NumeralMono.copy(fontSize = 14.sp, color = SpektrumAccent),
            )
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { animPct },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SpektrumAccent,
                trackColor = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$animXp XP",
                style = MutedMono,
                color = SpektrumMuted,
            )
        }

        // T-minus chip
        Row(
            modifier = Modifier
                .clip(radius)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, radius)
                .clickable(onClick = onSinavClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                text = "T-${examDays.toString().padStart(2, '0')}",
                fontFamily = MonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(examReady.coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(
                            when {
                                examReady > 0.7f -> Success
                                examReady > 0.4f -> Warn
                                else             -> Danger
                            }
                        ),
                )
            }
        }

        // Zayıf konu chip (only shown when > 0)
        if (zayifKonu > 0) {
            Box(
                modifier = Modifier
                    .clip(radius)
                    .background(SpektrumStreakTint)
                    .border(1.dp, SpektrumStreak.copy(alpha = 0.33f), radius)
                    .clickable(onClick = onPratikClick)
                    .padding(horizontal = 11.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "×$zayifKonu",
                    fontFamily = MonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SpektrumStreak,
                )
            }
        }
    }
}
