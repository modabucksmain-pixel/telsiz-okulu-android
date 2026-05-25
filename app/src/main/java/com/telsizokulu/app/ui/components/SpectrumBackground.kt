package com.telsizokulu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun SpectrumBackground(accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(accent.copy(alpha = 0.06f), Color.Transparent),
                startY = 0f,
                endY = 200f,
            )
        )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            val bars = 60
            val barWidth = size.width / bars
            val mid = size.height / 2f
            for (i in 0 until bars) {
                val h = (7f + abs(sin(i * 0.42).toFloat() * 13f) + if (i % 7 == 0) 12f else 0f)
                val hPx = h.dp.toPx()
                val opacity = 0.16f + (i % 5) * 0.04f
                drawRect(
                    color = accent.copy(alpha = opacity),
                    topLeft = Offset(i * barWidth, mid - hPx / 2f),
                    size = Size(1.4.dp.toPx(), hPx),
                )
            }
        }
    }
}
