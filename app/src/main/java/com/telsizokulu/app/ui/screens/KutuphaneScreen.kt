package com.telsizokulu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.data.model.Kart
import com.telsizokulu.app.data.repository.CurriculumRepository
import com.telsizokulu.app.data.repository.ProgressRepository
import com.telsizokulu.app.ui.theme.*

private enum class KutuphaneSekme(val etiket: String) {
    NATO("NATO"), Q("Q Kodları"), MORSE("Morse"), PROSEDUR("Prosedür")
}

private val MORSE_MAP = mapOf(
    'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.",
    'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
    'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.",
    'S' to "...", 'T' to "-", 'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
    'Y' to "-.--", 'Z' to "--..",
)

private val PROSEDUR_TERIMLERI = listOf(
    "OVER"    to "Ben konuştum, sıra sende",
    "OUT"     to "Konuşma bitti, kapatıyorum",
    "ROGER"   to "Anlaşıldı",
    "WILCO"   to "Anlaşıldı, uygulayacağım",
    "COPY"    to "Aldım / anladım",
    "BREAK"   to "Acil bir ara talep",
    "CQ"      to "Genel çağrı",
    "MAYDAY"  to "Hayati tehlike — acil yardım",
    "PAN-PAN" to "Acil ama hayati değil",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KutuphaneScreen(
    curriculumRepo: CurriculumRepository,
    progressRepo: ProgressRepository,
    onGeri: () -> Unit,
) {
    var natoKartlar by remember { mutableStateOf<List<Kart>>(emptyList()) }
    var qKartlar    by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var sekme       by remember { mutableStateOf(KutuphaneSekme.NATO) }

    LaunchedEffect(Unit) {
        val nato = curriculumRepo.loadIcerik("nato.json").kartlar
            .filter { it.id.startsWith("nato_") && (it.on ?: "").length == 1 && (it.on ?: " ")[0].isLetter() }
            .sortedBy { it.on }
        natoKartlar = nato

        val qRegex = Regex("^Q[A-Z]{2,3}$")
        qKartlar = curriculumRepo.loadIcerik("qcodes.json").kartlar.mapNotNull { k ->
            val kod = listOf(k.on, k.arka).firstOrNull { it != null && qRegex.matches(it.trim()) }?.trim()
            val anlam = listOf(k.on, k.arka).firstOrNull { it != null && !qRegex.matches(it.trim()) && it.isNotBlank() }?.trim()
            if (kod != null && anlam != null) kod to anlam else null
        }
    }

    Scaffold(
        containerColor = SpektrumBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("REFERANS", style = EyebrowMono.copy(color = SpektrumAccent))
                        Text("Kütüphane", style = ScreenTitle, color = SpektrumOnSurface)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri", tint = SpektrumOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpektrumBg),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Segment sekme kontrolü ──────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SpektrumSurface)
                    .border(1.dp, SpektrumOutline, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                KutuphaneSekme.entries.forEach { s ->
                    val aktif = sekme == s
                    Text(
                        text = s.etiket,
                        fontFamily = MonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        color = if (aktif) SpektrumSurface else SpektrumMuted,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (aktif) SpektrumOnSurface else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { sekme = s }
                            .padding(vertical = 8.dp),
                    )
                }
            }

            when (sekme) {
                KutuphaneSekme.NATO -> NatoListe(natoKartlar)
                KutuphaneSekme.Q -> QListe(qKartlar)
                KutuphaneSekme.MORSE -> MorseListe(natoKartlar)
                KutuphaneSekme.PROSEDUR -> ProsedurListe()
            }
        }
    }
}

// ── NATO — 2 kolon grid ─────────────────────────────────────────────────────

@Composable
private fun NatoListe(kartlar: List<Kart>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(kartlar, key = { it.id }) { kart ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SpektrumSurface)
                    .border(1.dp, SpektrumOutline, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SpektrumAccentTint),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = kart.on ?: "",
                        fontFamily = MonoFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = SpektrumAccent,
                    )
                }
                Text(
                    text = kart.arka ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SpektrumOnSurface,
                )
            }
        }
    }
}

// ── Q Kodları — tek kolon liste ─────────────────────────────────────────────

@Composable
private fun QListe(kodlar: List<Pair<String, String>>) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(kodlar, key = { it.first }) { (kod, anlam) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SpektrumSurface)
                    .border(1.dp, SpektrumOutline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = kod,
                    fontFamily = MonoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp,
                    color = SpektrumAccent,
                    modifier = Modifier.width(56.dp),
                )
                Text(
                    text = anlam,
                    fontSize = 13.5.sp,
                    color = SpektrumOnSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// ── Morse — 4 kolon grid ────────────────────────────────────────────────────

@Composable
private fun MorseListe(kartlar: List<Kart>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(kartlar, key = { it.id }) { kart ->
            val harf = (kart.on ?: "").firstOrNull()?.uppercaseChar()
            val mors = harf?.let { MORSE_MAP[it] } ?: ""
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SpektrumSurface)
                    .border(1.dp, SpektrumOutline, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = kart.on ?: "",
                    fontFamily = MonoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = SpektrumOnSurface,
                )
                Text(
                    text = mors,
                    fontFamily = MonoFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = SpektrumAccent,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

// ── Prosedür — tek kolon liste ──────────────────────────────────────────────

@Composable
private fun ProsedurListe() {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(PROSEDUR_TERIMLERI, key = { it.first }) { (terim, anlam) ->
            val acil = terim == "MAYDAY" || terim == "PAN-PAN"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SpektrumSurface)
                    .border(
                        1.dp,
                        if (acil) Danger.copy(alpha = 0.5f) else SpektrumOutline,
                        RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = terim,
                    fontFamily = MonoFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    color = if (acil) Danger else SpektrumAccent,
                )
                Text(
                    text = anlam,
                    fontSize = 13.sp,
                    color = SpektrumOnSurface,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
