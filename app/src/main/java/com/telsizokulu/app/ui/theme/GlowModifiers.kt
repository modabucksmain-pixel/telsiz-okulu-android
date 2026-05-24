package com.telsizokulu.app.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Neon glow: vurgu renkli renkli gölge (API 28+) + ince accent kenar.
 * Koyu Spektrum temasında "ışıyan kenar" hissi verir.
 * API 26/27'de gölge rengi yok sayılır, kenar yine de kalır.
 */
fun Modifier.neonGlow(
    color: Color,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 14.dp,
    glowAlpha: Float = 0.55f,
    borderWidth: Dp = 1.dp,
    borderAlpha: Float = 0.65f,
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = color.copy(alpha = glowAlpha),
        spotColor = color.copy(alpha = glowAlpha),
    )
    .border(borderWidth, color.copy(alpha = borderAlpha), RoundedCornerShape(cornerRadius))
