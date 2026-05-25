package com.telsizokulu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.ui.theme.*

private data class OnboardingAdim(val ikon: String, val baslik: String, val metin: String, val renk: Color)

private val adimlar = listOf(
    OnboardingAdim("🗺️", "Bölüm Haritası", "NATO alfabesinden TRAC sınavına kadar 6 bölüm. Sırayla aç, ilerle.", SpektrumAccent),
    OnboardingAdim("📇", "Dersler", "Önce bilgi kartlarını çevir, sonra alıştırmalarla pekiştir.", Color(0xFF14B8A6)),
    OnboardingAdim("🎯", "Sınavlar", "Alt bölüm, bölüm ve genel TRAC sınavıyla kendini test et.", Color(0xFF7C3AED)),
    OnboardingAdim("🔁", "Pratik", "Yanlış yaptığın kartlar öne çıkar, zayıf konuları kapat.", Color(0xFFFF6B00)),
)

/**
 * İlk açılış tanıtım katmanı — tam ekran, neon adım kartları, atla/ileri/başla.
 */
@Composable
fun OnboardingOverlay(onKapat: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val adim = adimlar[index]
    val sonuncu = index == adimlar.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpektrumBg.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Atla
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onKapat) {
                    Text("Atla", color = SpektrumMuted, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .neonGlow(adim.renk, cornerRadius = 60.dp, elevation = 18.dp, glowAlpha = 0.5f, borderAlpha = 0.6f)
                    .clip(CircleShape)
                    .background(SpektrumSurface),
                contentAlignment = Alignment.Center,
            ) {
                Text(adim.ikon, fontSize = 56.sp)
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = adim.baslik,
                color = SpektrumOnSurface,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = adim.metin,
                color = SpektrumMuted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            // Adım göstergeleri
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                adimlar.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == index) 22.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (i == index) adim.renk else SpektrumMuted.copy(alpha = 0.35f)),
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { if (sonuncu) onKapat() else index++ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = adim.renk),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = if (sonuncu) "Başla 🚀" else "İleri ›",
                    color = SpektrumOnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
