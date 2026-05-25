package com.telsizokulu.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.telsizokulu.app.data.model.AltBolum
import com.telsizokulu.app.data.model.Bolum
import com.telsizokulu.app.data.model.KullaniciIlerleme
import com.telsizokulu.app.ui.components.ChapterSelectorCard
import com.telsizokulu.app.ui.components.ChapterShowcase
import com.telsizokulu.app.ui.components.CompactStatsStrip
import com.telsizokulu.app.ui.components.GrillMeBottomSheet
import com.telsizokulu.app.ui.components.GrillMeFab
import com.telsizokulu.app.ui.components.HomeLessonRow
import com.telsizokulu.app.ui.components.LessonStatus
import com.telsizokulu.app.ui.components.OnboardingOverlay
import com.telsizokulu.app.ui.components.NavTab
import com.telsizokulu.app.ui.components.StatChip
import com.telsizokulu.app.ui.components.StatChipKind
import com.telsizokulu.app.ui.components.TelsizBottomNav
import com.telsizokulu.app.ui.components.isChapterLocked
import com.telsizokulu.app.ui.theme.BolumColors
import com.telsizokulu.app.ui.theme.EyebrowMono
import com.telsizokulu.app.ui.theme.LessonTitle
import com.telsizokulu.app.ui.theme.MonoFamily
import com.telsizokulu.app.ui.theme.BodySmall
import com.telsizokulu.app.ui.theme.MutedMono
import com.telsizokulu.app.ui.theme.SpektrumAccent
import com.telsizokulu.app.ui.theme.SpektrumBg
import com.telsizokulu.app.ui.theme.SpektrumMuted
import com.telsizokulu.app.ui.theme.SpektrumOnSurface
import com.telsizokulu.app.ui.theme.SpektrumSurface
import com.telsizokulu.app.ui.theme.neonGlow
import com.telsizokulu.app.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onDersClick: (bolumId: String, altBolumId: String, dersNo: Int) -> Unit,
    onBolumClick: (String) -> Unit,
    onSinavMenuClick: () -> Unit,
    onProfilClick: () -> Unit,
    onKutuphaneClick: () -> Unit,
    onPratikClick: () -> Unit,
) {
    val uiState  by viewModel.uiState.collectAsState()
    val ilerleme  = uiState.ilerleme
    val bolumler  = uiState.curriculum.bolumler
    val selIdx    = uiState.selectedChapterIdx

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()

    // Flash overlay state
    var flashColor  by remember { mutableStateOf<Color?>(null) }
    var flashAlpha  by remember { mutableStateOf(0f) }
    val animAlpha   by animateFloatAsState(
        targetValue  = flashAlpha,
        animationSpec= tween(durationMillis = 400),
        label        = "flash",
    )

    // Grill Me state
    var grillOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SpektrumBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            TelsizBottomNav(
                current         = NavTab.HOME,
                onHomeClick     = { },
                onKutuphaneClick= onKutuphaneClick,
                onSinavClick    = onSinavMenuClick,
                onProfilClick   = onProfilClick,
            )
        },
    ) { paddingValues ->

        if (uiState.yukleniyor || bolumler.isEmpty()) {
            Box(
                Modifier.fillMaxSize().background(SpektrumBg),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Flash overlay (behind content, above background)
            flashColor?.let { fc ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(100f)
                        .drawBehind { drawRect(fc.copy(alpha = animAlpha)) },
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SpektrumBg)
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                // ── Top Bar ─────────────────────────────────────────
                item {
                    TopBar(
                        xp     = ilerleme.kullanici.xp,
                        streak = ilerleme.kullanici.streak,
                        seviye = uiState.seviye,
                    )
                }

                // ── Compact Stats Strip ──────────────────────────────
                item {
                    CompactStatsStrip(
                        xp          = ilerleme.kullanici.xp,
                        seviye      = uiState.seviye,
                        seviyePct   = uiState.seviyePct,
                        examDays    = 90,
                        examReady   = 0f,
                        zayifKonu   = uiState.zayifKonuSayisi,
                        onSinavClick= onSinavMenuClick,
                        onPratikClick= onPratikClick,
                    )
                }

                // ── Chapter Selector ─────────────────────────────────
                item {
                    ChapterSelectorCard(
                        bolumler          = bolumler,
                        selectedIdx       = selIdx,
                        ilerleme          = ilerleme,
                        bolumIlerlemeleri = uiState.bolumIlerlemeleri,
                        onSelect          = { idx ->
                            if (isChapterLocked(idx, bolumler, ilerleme)) {
                                val prevName = bolumler.getOrNull(idx - 1)?.baslik ?: ""
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Önce \"$prevName\" bölümünü tamamla"
                                    )
                                }
                            } else {
                                val accent = BolumColors.fromId(bolumler[idx].id)
                                scope.launch {
                                    flashColor = accent
                                    flashAlpha = 0.09f
                                    delay(400)
                                    flashAlpha = 0f
                                    delay(400)
                                    flashColor = null
                                }
                                viewModel.selectChapter(idx)
                            }
                        },
                    )
                }

                // ── Devam Et button ──────────────────────────────────
                uiState.nextLesson?.let { next ->
                    item {
                        val bolum  = bolumler.find { it.id == next.bolumId }
                        val accent = bolum?.let { BolumColors.fromId(it.id) } ?: MaterialTheme.colorScheme.primary
                        ContinueButton(
                            bolumRenk = accent,
                            dersBaslik= next.baslik,
                            onClick   = { onDersClick(next.bolumId, next.altBolumId, next.dersNo) },
                        )
                    }
                }

                // ── Chapter Showcase ─────────────────────────────────
                bolumler.getOrNull(selIdx)?.let { bolum ->
                    item {
                        ChapterShowcase(bolum = bolum)
                    }

                    // ── Lessons header ───────────────────────────────
                    item {
                        val pct = uiState.bolumIlerlemeleri[bolum.id] ?: 0
                        LessonsHeader(bolum = bolum, pct = pct)
                    }

                    // ── Flat lesson list ─────────────────────────────
                    val lessonItems = buildLessonItems(bolum, ilerleme)
                    items(lessonItems, key = { it.key }) { item ->
                        when (item) {
                            is LessonItem.Ders -> HomeLessonRow(
                                no        = item.no,
                                baslik    = item.baslik,
                                isSinav   = false,
                                status    = item.status,
                                bolumRenk = BolumColors.fromId(bolum.id),
                                onClick   = {
                                    if (item.status != LessonStatus.LOCKED)
                                        onDersClick(bolum.id, item.altBolumId, item.dersNo)
                                },
                                modifier  = Modifier.padding(horizontal = 16.dp),
                            )
                            is LessonItem.Sinav -> HomeLessonRow(
                                no        = 0,
                                baslik    = item.baslik,
                                isSinav   = true,
                                status    = item.status,
                                bolumRenk = BolumColors.fromId(bolum.id),
                                onClick   = {
                                    if (item.status != LessonStatus.LOCKED) {
                                        if (item.altBolumId != null)
                                            onBolumClick(bolum.id)  // navigate to BolumScreen for exam
                                        else
                                            onBolumClick(bolum.id)
                                    }
                                },
                                modifier  = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }
                }

                // ── Daily Morse ──────────────────────────────────────
                item { DailyMorseCard() }

                item { Spacer(Modifier.height(24.dp)) }
            }

            // ── Grill Me FAB ─────────────────────────────────────────
            GrillMeFab(
                onClick  = { grillOpen = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp + paddingValues.calculateBottomPadding()),
            )
        }

        // ── Grill Me Bottom Sheet ────────────────────────────────────
        if (grillOpen) {
            val bolum  = bolumler.getOrNull(selIdx)
            val bolumId= bolum?.id ?: "bolum_1"
            GrillMeBottomSheet(
                bolumId  = bolumId,
                bolumKod = BolumColors.kodFromId(bolumId),
                bolumRenk= BolumColors.fromId(bolumId),
                onDismiss= { grillOpen = false },
            )
        }

        // ── İlk açılış öğretici ──────────────────────────────────────
        if (uiState.onboardingGoster) {
            OnboardingOverlay(onKapat = { viewModel.onboardingKapat() })
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────

@Composable
private fun TopBar(xp: Int, streak: Int, seviye: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioLogo()
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "TELSIZ.OKULU",
                    style = MutedMono.copy(letterSpacing = 1.5.sp),
                    color = SpektrumMuted,
                )
                Text(
                    text = "TA2/CALL · Lv.$seviye",
                    style = LessonTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            StatChip(StatChipKind.STREAK, streak)
            StatChip(StatChipKind.XP,     xp)
        }
    }
}

@Composable
private fun RadioLogo() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(10.dp),
            )
            .then(
                Modifier.drawBehind {
                    // border
                    val stroke = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    drawRoundRect(
                        color       = Color(0xFF1F2937),
                        cornerRadius= androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                        style       = stroke,
                    )
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Radyatif anten dalgaları (handoff SVG'si 1:1)
        Canvas(modifier = Modifier.size(20.dp)) {
            val s = size.minDimension / 24f
            val sw = 1.5f * s
            val st = Stroke(width = sw, cap = StrokeCap.Round)
            val wave1 = Path().apply {
                moveTo(2 * s, 14 * s)
                cubicTo(4 * s, 10 * s, 8 * s, 7 * s, 12 * s, 7 * s)
                cubicTo(16 * s, 7 * s, 20 * s, 10 * s, 22 * s, 14 * s)
            }
            val wave2 = Path().apply {
                moveTo(5 * s, 16 * s)
                cubicTo(6.5f * s, 13 * s, 9 * s, 11 * s, 12 * s, 11 * s)
                cubicTo(15 * s, 11 * s, 17.5f * s, 13 * s, 19 * s, 16 * s)
            }
            val wave3 = Path().apply {
                moveTo(8 * s, 18 * s)
                cubicTo(9 * s, 16 * s, 10.5f * s, 15 * s, 12 * s, 15 * s)
                cubicTo(13.5f * s, 15 * s, 15 * s, 16 * s, 16 * s, 18 * s)
            }
            drawPath(wave1, SpektrumAccent, style = st)
            drawPath(wave2, SpektrumAccent.copy(alpha = 0.55f), style = st)
            drawPath(wave3, SpektrumAccent.copy(alpha = 0.30f), style = st)
            drawCircle(SpektrumAccent, radius = 1.5f * s, center = Offset(12 * s, 20 * s))
        }
    }
}

// ── Devam Et button ───────────────────────────────────────────────────────

@Composable
private fun ContinueButton(bolumRenk: Color, dersBaslik: String, onClick: () -> Unit) {
    Surface(
        onClick    = onClick,
        color      = MaterialTheme.colorScheme.onSurface,
        shape      = RoundedCornerShape(14.dp),
        modifier   = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
            .neonGlow(bolumRenk, cornerRadius = 14.dp, elevation = 14.dp, glowAlpha = 0.45f, borderAlpha = 0.5f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(bolumRenk, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint      = Color.White,
                    modifier  = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = "DEVAM ET",
                    style = EyebrowMono.copy(color = bolumRenk),
                )
                Text(
                    text  = dersBaslik,
                    style = LessonTitle,
                    color = MaterialTheme.colorScheme.surface,
                )
            }
            Text("→", color = bolumRenk, fontSize = 18.sp)
        }
    }
}

// ── Lessons Header ────────────────────────────────────────────────────────

@Composable
private fun LessonsHeader(bolum: Bolum, pct: Int) {
    val accent = BolumColors.fromId(bolum.id)
    val totalDers = bolum.altBolumler.sumOf { it.dersler.size }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
    ) {
        Text(
            text  = "DERSLER · $totalDers",
            style = MutedMono,
            color = SpektrumMuted,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pct / 100f)
                        .height(4.dp)
                        .background(accent, RoundedCornerShape(2.dp)),
                )
            }
            Text(
                text  = "$pct%",
                style = MutedMono,
                color = SpektrumMuted,
            )
        }
    }
}

// ── Lesson list data model ─────────────────────────────────────────────────

private sealed class LessonItem {
    abstract val key: String

    data class Ders(
        val altBolumId: String,
        val dersNo: Int,
        val no: Int,
        val baslik: String,
        val status: LessonStatus,
    ) : LessonItem() {
        override val key = "ders_${altBolumId}_$dersNo"
    }

    data class Sinav(
        val altBolumId: String?,
        val baslik: String,
        val status: LessonStatus,
    ) : LessonItem() {
        override val key = "sinav_${altBolumId ?: "bolum"}"
    }
}

private fun buildLessonItems(bolum: Bolum, ilerleme: KullaniciIlerleme): List<LessonItem> {
    val bolumKilitli = ilerleme.bolumler[bolum.id]?.durum == "kilitli"
    val result       = mutableListOf<LessonItem>()
    var globalNo     = 1

    bolum.altBolumler.forEachIndexed { abIdx, altBolum ->
        var prevDone = true
        altBolum.dersler.forEachIndexed { dIdx, ders ->
            val done = ilerleme.bolumler[bolum.id]
                ?.altBolumler?.get(altBolum.id)
                ?.tamamlananDersler?.contains(ders.no) == true
            val status = when {
                bolumKilitli           -> LessonStatus.LOCKED
                done                   -> LessonStatus.DONE
                dIdx == 0 && abIdx == 0-> LessonStatus.ACCESSIBLE
                prevDone               -> LessonStatus.ACCESSIBLE
                else                   -> LessonStatus.LOCKED
            }
            result.add(LessonItem.Ders(altBolum.id, ders.no, globalNo++, ders.baslik, status))
            prevDone = done
        }

        // Alt-bolum exam row
        val abSinavDone   = ilerleme.bolumler[bolum.id]?.altBolumler?.get(altBolum.id)?.durum == "tamamlandi"
        val allDersDone   = altBolum.dersler.all { ders ->
            ilerleme.bolumler[bolum.id]?.altBolumler?.get(altBolum.id)
                ?.tamamlananDersler?.contains(ders.no) == true
        }
        val abSinavStatus = when {
            bolumKilitli  -> LessonStatus.LOCKED
            abSinavDone   -> LessonStatus.DONE
            allDersDone   -> LessonStatus.ACCESSIBLE
            else          -> LessonStatus.LOCKED
        }
        result.add(LessonItem.Sinav(altBolum.id, "${altBolum.baslik} Sınavı", abSinavStatus))
    }

    // Chapter final exam
    val gecmeSinavDone = ilerleme.bolumler[bolum.id]?.gecmeSinavi?.durum == "tamamlandi"
    val allAbDone      = bolum.altBolumler.all { ab ->
        ilerleme.bolumler[bolum.id]?.altBolumler?.get(ab.id)?.durum == "tamamlandi"
    }
    val finalStatus = when {
        bolumKilitli  -> LessonStatus.LOCKED
        gecmeSinavDone-> LessonStatus.DONE
        allAbDone     -> LessonStatus.ACCESSIBLE
        else          -> LessonStatus.LOCKED
    }
    result.add(LessonItem.Sinav(null, "Bölüm Final Sınavı", finalStatus))

    return result
}

// ── Daily Morse ───────────────────────────────────────────────────────────

private data class GununBilgisi(val kod: String, val metin: String)

private val GUNUN_BILGISI = listOf(
    GununBilgisi("QSY", "QSY = \"Frekans değiştir\". QSO ortasında uzlaşılan yeni frekansa geçişte kullanılır."),
    GununBilgisi("73",  "73 = \"İyi şanslar / selamlar\". QSO sonunda kullanılan en yaygın kapanış kodudur."),
    GununBilgisi("CQ",  "CQ = \"Genel çağrı\". Cevap verebilecek herhangi bir istasyona yapılan açık davet."),
    GununBilgisi("SOS", "SOS Morse: ··· ——— ··· — Üç kısa, üç uzun, üç kısa; aralıksız okunur."),
)

private val MORSE_LETTERS = mapOf(
    'A' to ".-",   'B' to "-...", 'C' to "-.-.", 'D' to "-..",  'E' to ".",
    'F' to "..-.", 'G' to "--.",  'H' to "....", 'I' to "..",   'J' to ".---",
    'K' to "-.-",  'L' to ".-..", 'M' to "--",   'N' to "-.",   'O' to "---",
    'P' to ".--.", 'Q' to "--.-", 'R' to ".-.",  'S' to "...",  'T' to "-",
    'U' to "..-",  'V' to "...-", 'W' to ".--",  'X' to "-..-", 'Y' to "-.--",
    'Z' to "--..",
    '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
    '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
)

private fun morseEncode(text: String): String =
    text.uppercase().map { c ->
        when {
            c == ' ' -> " "
            MORSE_LETTERS.containsKey(c) -> MORSE_LETTERS[c]!!
            else -> "?"
        }
    }.joinToString(" / ")

@Composable
private fun DailyMorseCard() {
    val dayIndex = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH) % GUNUN_BILGISI.size
    val bilgi    = GUNUN_BILGISI[dayIndex]
    val morse    = morseEncode(bilgi.kod)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .neonGlow(SpektrumAccent, cornerRadius = 14.dp, elevation = 8.dp, glowAlpha = 0.25f, borderAlpha = 0.4f)
            .clip(RoundedCornerShape(14.dp))
            .background(SpektrumSurface)
            .padding(16.dp),
    ) {
        // Eyebrow
        Text(
            text  = "GÜNÜN BİLGİSİ · ${bilgi.kod}",
            style = EyebrowMono,
            color = SpektrumMuted,
        )
        Spacer(Modifier.height(8.dp))
        // Morse encoding box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SpektrumBg)
                .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text        = morse,
                fontFamily  = MonoFamily,
                fontWeight  = FontWeight.Bold,
                fontSize    = 20.sp,
                letterSpacing = 2.sp,
                color       = SpektrumAccent,
                textAlign   = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(8.dp))
        // Description
        Text(
            text  = bilgi.metin,
            style = BodySmall,
            color = SpektrumOnSurface.copy(alpha = 0.75f),
        )
    }
}
