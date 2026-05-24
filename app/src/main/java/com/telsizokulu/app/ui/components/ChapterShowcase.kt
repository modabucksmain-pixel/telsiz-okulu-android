package com.telsizokulu.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.data.model.Bolum
import com.telsizokulu.app.ui.theme.BolumColors
import com.telsizokulu.app.ui.theme.BodySmall
import com.telsizokulu.app.ui.theme.ChapterName
import com.telsizokulu.app.ui.theme.Danger
import com.telsizokulu.app.ui.theme.EyebrowMono
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.MutedMono
import com.telsizokulu.app.ui.theme.NumeralMono
import com.telsizokulu.app.ui.theme.SpektrumMuted
import com.telsizokulu.app.ui.theme.Success

private val natoAlphabet = listOf(
    "A" to "Alfa",    "B" to "Bravo",   "C" to "Charlie", "D" to "Delta",
    "E" to "Echo",    "F" to "Foxtrot", "G" to "Golf",    "H" to "Hotel",
    "I" to "India",   "J" to "Juliet",  "K" to "Kilo",    "L" to "Lima",
    "M" to "Mike",    "N" to "November","O" to "Oscar",   "P" to "Papa",
    "Q" to "Quebec",  "R" to "Romeo",   "S" to "Sierra",  "T" to "Tango",
    "U" to "Uniform", "V" to "Victor",  "W" to "Whiskey", "X" to "X-ray",
    "Y" to "Yankee",  "Z" to "Zulu",
)

private val qCodeSamples = listOf(
    "QRZ" to "Kim arıyor?",
    "QSL" to "Alındı",
    "QSO" to "İletişim",
    "QTH" to "Konum",
    "QRM" to "Parazit",
    "QRP" to "Gücü azalt",
)

private val freqBands = listOf(
    "80m" to 3.5,  "40m" to 7.0,  "20m" to 14.0,
    "15m" to 21.0, "10m" to 28.0, "2m"  to 144.0, "70cm" to 435.0,
)

private val prosedurTerms = listOf(
    "OVER"   to "Dinliyorum",
    "ROGER"  to "Anlaşıldı",
    "WILCO"  to "Yapacağım",
    "BREAK"  to "Ara veriyorum",
    "OUT"    to "Bitti",
)

@Composable
fun ChapterShowcase(bolum: Bolum, modifier: Modifier = Modifier) {
    val accent = BolumColors.fromId(bolum.id)
    val kod    = BolumColors.kodFromId(bolum.id)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
    ) {
        SpectrumBackground(accent = accent, modifier = Modifier.matchParentSize())
        Column(modifier = Modifier.padding(16.dp)) {
            // Eyebrow header
            Text(
                text = kod,
                style = EyebrowMono.copy(color = accent),
            )
            Spacer(Modifier.height(10.dp))
            when (bolum.id) {
                "bolum_1" -> NatoShowcase(accent)
                "bolum_2" -> QCodeShowcase(accent)
                "bolum_3" -> ElektronikShowcase(accent)
                "bolum_4" -> FrekansShowcase(accent)
                "bolum_5" -> ProsedurShowcase(accent)
                "bolum_6" -> TracShowcase(accent)
                else      -> DefaultShowcase(bolum, accent)
            }
        }
    }
}

// ── BOLUM 1: NATO ─────────────────────────────────────────────────────────

@Composable
private fun NatoShowcase(accent: Color) {
    var selectedIdx by remember { mutableStateOf(0) }
    val selected = natoAlphabet[selectedIdx]

    // Selected letter card
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, accent, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = selected.first,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            color = accent,
            modifier = Modifier.width(56.dp),
            textAlign = TextAlign.Center,
        )
        Column {
            Text(
                text = selected.second,
                style = ChapterName.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${selected.second.uppercase()} → ${selected.first}",
                style = MutedMono,
                color = SpektrumMuted,
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    // Alphabet grid — non-lazy 9-col grid (26 letters, 3 rows)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        natoAlphabet.chunked(9).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEachIndexed { colIdx, pair ->
                    val i = natoAlphabet.indexOf(pair)
                    val isSelected = i == selectedIdx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (isSelected) accent else accent.copy(alpha = 0.086f))
                            .border(
                                1.dp,
                                if (isSelected) accent else accent.copy(alpha = 0.25f),
                                RoundedCornerShape(5.dp),
                            )
                            .clickable { selectedIdx = i },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = pair.first,
                            fontFamily = MonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White else accent,
                        )
                    }
                }
                // Fill remaining cells in last row
                repeat(9 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ── BOLUM 2: Q KODU ───────────────────────────────────────────────────────

@Composable
private fun QCodeShowcase(accent: Color) {
    // Sample QSO transcript
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Column {
            QsoLine("TA1ABC", "QRZ DE TA1ABC, QSL?", accent, true)
            QsoLine("DL2XYZ", "TA1ABC DE DL2XYZ, QSL, QTH Stuttgart", accent, false)
            QsoLine("TA1ABC", "QSL, 73 DE TA1ABC, OUT", accent, true)
        }
    }

    Spacer(Modifier.height(10.dp))

    // 6 Q-code cards
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(qCodeSamples) { (kod, anlam) ->
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(accent.copy(alpha = 0.086f))
                    .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(
                    text = kod,
                    fontFamily = MonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accent,
                )
                Text(
                    text = anlam,
                    style = BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun QsoLine(callsign: String, metin: String, accent: Color, isTa: Boolean) {
    Row(modifier = Modifier.padding(vertical = 1.dp)) {
        Text(
            text = callsign,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = if (isTa) accent else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(56.dp),
        )
        Text(
            text = metin,
            fontFamily = MonoFamily,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── BOLUM 3: ELEKTRONİK ──────────────────────────────────────────────────

@Composable
private fun ElektronikShowcase(accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OhmTriangle(accent = accent, modifier = Modifier.size(96.dp, 90.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("V = I · R", "I = V / R", "R = V / I", "P = V · I").forEach { formula ->
                Text(
                    text = formula,
                    fontFamily = MonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = accent,
                )
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf("V" to "Volt", "A" to "Amper", "Ω" to "Ohm", "W" to "Watt").forEach { (symbol, unit) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(7.dp))
                    .background(accent.copy(alpha = 0.086f))
                    .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(7.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = symbol,
                    fontFamily = MonoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = accent,
                )
                Text(
                    text = unit,
                    style = BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun OhmTriangle(accent: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.5f, 6.dp.toPx())
                lineTo(w * 0.94f, h - 8.dp.toPx())
                lineTo(w * 0.06f, h - 8.dp.toPx())
                close()
            }
            drawPath(path, accent.copy(alpha = 0.12f))
            drawPath(path, accent, style = Stroke(2.dp.toPx()))
            drawLine(
                accent.copy(alpha = 0.4f),
                Offset(w * 0.06f, h * 0.62f),
                Offset(w * 0.94f, h * 0.62f),
                strokeWidth = 1.5.dp.toPx(),
            )
            drawLine(
                accent.copy(alpha = 0.4f),
                Offset(w * 0.5f, h * 0.62f),
                Offset(w * 0.5f, h - 8.dp.toPx()),
                strokeWidth = 1.5.dp.toPx(),
            )
        }
        Text(
            "V", color = accent, fontFamily = MonoFamily, fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
        )
        Text(
            "I", color = accent, fontFamily = MonoFamily, fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 14.dp, bottom = 14.dp),
        )
        Text(
            "R", color = accent, fontFamily = MonoFamily, fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 14.dp, bottom = 14.dp),
        )
    }
}

// ── BOLUM 4: FREKANS ─────────────────────────────────────────────────────

@Composable
private fun FrekansShowcase(accent: Color) {
    var selectedBandIdx by remember { mutableStateOf(2) }  // default 20m
    val (band, mhz) = freqBands[selectedBandIdx]

    // Selected band display
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mhz.toInt().toString(),
            style = NumeralMono.copy(fontSize = 32.sp, color = accent),
        )
        Text(
            text = " MHz",
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = accent.copy(alpha = 0.7f),
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "BAND $band",
            fontFamily = MonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(accent.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }

    Spacer(Modifier.height(10.dp))

    // Band selector chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        freqBands.forEachIndexed { i, (b, _) ->
            val selected = i == selectedBandIdx
            Text(
                text = b,
                fontFamily = MonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (selected) Color.White else accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (selected) accent else accent.copy(alpha = 0.086f))
                    .border(1.dp, accent.copy(alpha = if (selected) 1f else 0.25f), RoundedCornerShape(5.dp))
                    .clickable { selectedBandIdx = i }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

// ── BOLUM 5: PROSEDÜR ─────────────────────────────────────────────────────

@Composable
private fun ProsedurShowcase(accent: Color) {
    // CQ sample
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(10.dp),
    ) {
        Text(
            text = "CQ CQ CQ DE TA1ABC TA1ABC\nQRZ? OVER",
            fontFamily = MonoFamily,
            fontSize = 11.sp,
            color = accent,
            lineHeight = 17.sp,
        )
    }

    Spacer(Modifier.height(10.dp))

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        prosedurTerms.forEach { (term, anlam) ->
            val isEmergency = term == "MAYDAY" || term == "PAN-PAN"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (isEmergency) Danger.copy(alpha = 0.086f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        1.dp,
                        if (isEmergency) Danger.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(7.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = term,
                    fontFamily = MonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isEmergency) Danger else accent,
                    modifier = Modifier.width(72.dp),
                )
                Text(
                    text = anlam,
                    style = BodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── BOLUM 6: TRAC ─────────────────────────────────────────────────────────

@Composable
private fun TracShowcase(accent: Color) {
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "T-90",
                style = NumeralMono.copy(fontSize = 28.sp, color = accent),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "GÜN",
                fontFamily = MonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = accent.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { 0.3f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = accent,
            trackColor = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("0" to "Deneme", "0%" to "Ort.Puan", "0" to "Zayıf").forEach { (val_, lbl) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(7.dp))
                        .background(accent.copy(alpha = 0.086f))
                        .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(7.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        val_,
                        fontFamily = MonoFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = accent,
                    )
                    Text(
                        lbl,
                        style = BodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(10.dp),
        ) {
            Text(
                text = "İPUCU: TRAC sınavı 40 sorulu çoktan seçmeli. Geçme notu 60/100.",
                style = BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── FALLBACK ──────────────────────────────────────────────────────────────

@Composable
private fun DefaultShowcase(bolum: Bolum, accent: Color) {
    Text(
        text = bolum.aciklama,
        style = BodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
