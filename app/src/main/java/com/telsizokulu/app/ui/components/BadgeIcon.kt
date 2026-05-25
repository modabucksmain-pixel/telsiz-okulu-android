package com.telsizokulu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.telsizokulu.app.engine.GamificationEngine
import com.telsizokulu.app.ui.theme.AmberWarning
import com.telsizokulu.app.ui.theme.Blue60
import com.telsizokulu.app.ui.theme.GoldXP
import com.telsizokulu.app.ui.theme.GreenSuccess
import com.telsizokulu.app.ui.theme.OrangeStreak
import com.telsizokulu.app.ui.theme.Purple60
import com.telsizokulu.app.ui.theme.Slate700
import com.telsizokulu.app.ui.theme.Slate950
import com.telsizokulu.app.ui.theme.neonGlow

/** Rozet id'sine göre tematik accent renk. */
fun rozetRenk(rozetId: String): Color = when (rozetId) {
    "ilk_adim"          -> Blue60
    "nato_caylagi", "nato_ustasi" -> Blue60
    "q_kodu_bilgesi"    -> Color(0xFF14B8A6)
    "elektronik_dehasi" -> AmberWarning
    "frekans_avcisi"    -> Color(0xFF0EA5E9)
    "prosedur_ustasi"   -> GreenSuccess
    "7_gun_seri", "30_gun_seri" -> OrangeStreak
    "mukemmeliyetci"    -> GoldXP
    "trac_adayi"        -> Purple60
    else                -> Blue60
}

/**
 * Neon rozet ikonu: tematik renkli halka + glow + ortada emoji.
 * Kazanılmadıysa soluk/gri gösterilir.
 */
@Composable
fun BadgeIcon(
    rozetId: String,
    kazanildi: Boolean = true,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier,
) {
    val emoji = GamificationEngine.ROZET_BILGILERI[rozetId]?.first ?: "🏅"
    val accent = if (kazanildi) rozetRenk(rozetId) else Slate700

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (kazanildi) Modifier.neonGlow(accent, cornerRadius = size / 2, elevation = 12.dp, glowAlpha = 0.5f, borderAlpha = 0.7f)
                else Modifier
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(Slate950, accent.copy(alpha = if (kazanildi) 0.18f else 0.05f))
                )
            )
            .alpha(if (kazanildi) 1f else 0.45f),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = (size.value * 0.42f).sp, fontWeight = FontWeight.Bold)
    }
}
