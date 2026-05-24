package com.telsizokulu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.ui.components.SpectrumBackground
import com.telsizokulu.app.ui.theme.BolumColors
import com.telsizokulu.app.ui.theme.BodySmall
import com.telsizokulu.app.ui.theme.Danger
import com.telsizokulu.app.ui.theme.EyebrowMono
import com.telsizokulu.app.ui.theme.LessonTitle
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.MutedMono
import com.telsizokulu.app.ui.theme.NumeralMono
import com.telsizokulu.app.ui.theme.SansFamily
import com.telsizokulu.app.ui.theme.SpektrumBg
import com.telsizokulu.app.ui.theme.SpektrumMuted
import com.telsizokulu.app.ui.theme.SpektrumStreak
import com.telsizokulu.app.ui.theme.SpektrumStreakTint
import com.telsizokulu.app.ui.theme.Success
import com.telsizokulu.app.ui.theme.Warn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinavMenuScreen(
    onGeri: () -> Unit,
    onGenelSinavClick: () -> Unit,
) {
    val accent = BolumColors.Trac

    Scaffold(
        containerColor = SpektrumBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SINAV",
                        style = EyebrowMono.copy(letterSpacing = 2.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SpektrumBg,
                ),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SpektrumBg)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { TminusHeroCard(accent = accent) }

            item {
                SinavRow(
                    icon     = "📋",
                    baslik   = "Tam Deneme Sınavı",
                    alt      = "40 soru · gerçek format · 60 dk",
                    cta      = "BAŞLA",
                    ctaColor = accent,
                    onClick  = onGenelSinavClick,
                )
            }
            item {
                SinavRow(
                    icon    = "📚",
                    baslik  = "Bölüm Sınavı",
                    alt     = "Tek bölüm · 15–20 soru",
                    cta     = "SEÇ",
                    ctaColor= BolumColors.Nato,
                    onClick = { /* navigate to bolum picker */ },
                )
            }
            item {
                SinavRow(
                    icon    = "⚡",
                    baslik  = "Hızlı 10",
                    alt     = "10 soru · ~5 dk",
                    cta     = "BAŞLA",
                    ctaColor= BolumColors.Frekans,
                    onClick = onGenelSinavClick,
                )
            }
            item {
                SinavRow(
                    icon    = "📁",
                    baslik  = "Çıkmış Sorular",
                    alt     = "Geçmiş sınav arşivi",
                    cta     = "AÇ",
                    ctaColor= BolumColors.QCode,
                    onClick = { },
                )
            }
            item {
                SinavRow(
                    icon    = "🎯",
                    baslik  = "Zayıf Konu Sınavı",
                    alt     = "Öğrenemediklerinize odaklan",
                    cta     = "ÇALIŞ",
                    ctaColor= SpektrumStreak,
                    onClick = { },
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun TminusHeroCard(accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
    ) {
        SpectrumBackground(accent = accent, modifier = Modifier.matchParentSize())
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "SINAVA HAZIRLIK",
                style = EyebrowMono.copy(color = accent),
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text  = "T-90",
                    style = NumeralMono.copy(fontSize = 36.sp, color = accent),
                )
                Text(
                    text  = " GÜN",
                    fontFamily = MonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    letterSpacing = 1.sp,
                    color      = accent.copy(alpha = 0.7f),
                    modifier   = Modifier.padding(bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 0.3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color      = accent,
                trackColor = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "0"  to "Deneme",
                    "0%" to "Ort.Puan",
                    "0"  to "Zayıf konu",
                ).forEach { (value, label) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(accent.copy(alpha = 0.086f))
                            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = value,
                            fontFamily = MonoFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 18.sp,
                            color      = accent,
                        )
                        Text(
                            text  = label,
                            style = BodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SinavRow(
    icon: String,
    baslik: String,
    alt: String,
    cta: String,
    ctaColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 22.sp, modifier = Modifier.padding(end = 12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = baslik,
                style = LessonTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text  = alt,
                style = BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text         = cta,
            fontFamily   = MonoFamily,
            fontWeight   = FontWeight.Bold,
            fontSize     = 10.sp,
            letterSpacing= 1.sp,
            color        = ctaColor,
            modifier     = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(ctaColor.copy(alpha = 0.12f))
                .border(1.dp, ctaColor.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
