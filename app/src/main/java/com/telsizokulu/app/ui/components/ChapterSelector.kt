package com.telsizokulu.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.data.model.Bolum
import com.telsizokulu.app.data.model.KullaniciIlerleme
import com.telsizokulu.app.ui.theme.BolumColors
import com.telsizokulu.app.ui.theme.ChapterName
import com.telsizokulu.app.ui.theme.EyebrowMono
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.MutedMono
import com.telsizokulu.app.ui.theme.SpektrumMuted

@Composable
fun ChapterSelectorCard(
    bolumler: List<Bolum>,
    selectedIdx: Int,
    ilerleme: KullaniciIlerleme,
    bolumIlerlemeleri: Map<String, Int>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (bolumler.isEmpty()) return
    val bolum = bolumler[selectedIdx]
    val accent = BolumColors.fromId(bolum.id)
    val kod = BolumColors.kodFromId(bolum.id)
    val progress = (bolumIlerlemeleri[bolum.id] ?: 0) / 100f
    val doneCount = bolumler.map { b ->
        val pct = bolumIlerlemeleri[b.id] ?: 0
        pct
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .drawBehind {
                drawRect(
                    color = accent,
                    size = Size(3.dp.toPx(), size.height),
                )
            },
    ) {
        SpectrumBackground(accent = accent, modifier = Modifier.matchParentSize())

        Column(modifier = Modifier.padding(14.dp)) {
            // Arrow row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                NavArrowButton(
                    label = "←",
                    enabled = selectedIdx > 0,
                    onClick = { onSelect(selectedIdx - 1) },
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "BÖLÜM ${bolum.siralama.toString().padStart(2, '0')} / 06 · $kod",
                        style = EyebrowMono.copy(color = accent),
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = bolum.baslik,
                        style = ChapterName,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                NavArrowButton(
                    label = "→",
                    enabled = selectedIdx < bolumler.size - 1,
                    onClick = { onSelect(selectedIdx + 1) },
                )
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.outline,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MutedMono,
                    color = SpektrumMuted,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Chapter dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                bolumler.forEachIndexed { i, b ->
                    val isActive = i == selectedIdx
                    val isLocked = isChapterLocked(i, bolumler, ilerleme)
                    val bAccent = BolumColors.fromId(b.id)
                    val bPct = bolumIlerlemeleri[b.id] ?: 0
                    val dotWidth = when {
                        isActive -> 28.dp
                        isLocked -> 8.dp
                        else     -> 10.dp
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.5.dp)
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isActive -> bAccent
                                    isLocked -> MaterialTheme.colorScheme.outlineVariant
                                    bPct > 0 -> bAccent.copy(alpha = 0.4f)
                                    else     -> MaterialTheme.colorScheme.outline
                                }
                            )
                            .clickable { onSelect(i) },
                    )
                }
            }
        }
    }
}

fun isChapterLocked(idx: Int, bolumler: List<Bolum>, ilerleme: KullaniciIlerleme): Boolean {
    if (idx == 0) return false
    val prevId = bolumler[idx - 1].id
    return ilerleme.bolumler[prevId]?.durum != "tamamlandi"
}

@Composable
private fun NavArrowButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .border(
                1.dp,
                if (enabled) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                RoundedCornerShape(9.dp),
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.outlineVariant,
        )
    }
}
