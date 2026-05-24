package com.telsizokulu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.telsizokulu.app.ui.theme.Blue60
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.Slate950
import com.telsizokulu.app.ui.theme.neonGlow
import kotlin.math.sin

/**
 * NATO harfi için özgün, neon temalı monogram.
 * Her harf benzersiz görünür (jenerik PNG yerine): büyük harf + altında sinyal dalgası.
 */
@Composable
fun NatoMonogram(
    harf: String,
    accent: Color = Blue60,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 140.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .neonGlow(accent, cornerRadius = 20.dp, elevation = 16.dp, glowAlpha = 0.5f, borderAlpha = 0.55f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Slate950, accent.copy(alpha = 0.10f))
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = harf,
                fontFamily = MonoFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 64.sp,
                color = accent,
            )
            Spacer(Modifier.height(8.dp))
            // Sinyal dalgası
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(horizontal = 20.dp)
            ) {
                val w = this.size.width
                val h = this.size.height
                val mid = h / 2f
                val path = Path().apply {
                    moveTo(0f, mid)
                    val steps = 40
                    for (i in 0..steps) {
                        val x = w * i / steps
                        val y = mid - sin(i / steps.toFloat() * 4f * Math.PI).toFloat() * (mid * 0.8f)
                        lineTo(x, y)
                    }
                }
                drawPath(path, color = accent.copy(alpha = 0.7f), style = Stroke(width = 2.dp.toPx()))
            }
        }
    }
}
