package com.telsizokulu.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.engine.GamificationEngine
import com.telsizokulu.app.ui.components.SpectrumBackground
import com.telsizokulu.app.ui.theme.*
import com.telsizokulu.app.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onGeri: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportJson { json ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    viewModel.importJson(input.bufferedReader().use { it.readText() })
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    if (uiState.yukleniyor) {
        Box(Modifier.fillMaxSize().background(SpektrumBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SpektrumAccent)
        }
        return
    }

    val ilerleme  = uiState.ilerleme
    val kullanici = ilerleme.kullanici
    val rozetTanimlari = uiState.rozetTanimlari
    val kazanilanRozetler = kullanici.rozetler

    val seviye = when {
        kullanici.xp >= 5000 -> 7; kullanici.xp >= 3500 -> 6
        kullanici.xp >= 2000 -> 5; kullanici.xp >= 1000 -> 4
        kullanici.xp >= 500  -> 3; kullanici.xp >= 200  -> 2; else -> 1
    }
    val seviyeIsim   = GamificationEngine.SEVIYE_ISIMLERI[seviye] ?: "Acemi Telsizci"
    val sonrakiXP    = when (seviye) { 1->200; 2->500; 3->1000; 4->2000; 5->3500; 6->5000; else->5000 }
    val mevcutTabanXP= when (seviye) { 1->0;   2->200; 3->500;  4->1000; 5->2000; 6->3500; else->5000 }
    val xpYuzde      = if (sonrakiXP == mevcutTabanXP) 1f
                       else (kullanici.xp - mevcutTabanXP).toFloat() / (sonrakiXP - mevcutTabanXP)

    Scaffold(
        containerColor = SpektrumBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("OPERATÖR", style = EyebrowMono.copy(color = SpektrumAccent))
                        Text("Profil", style = ScreenTitle, color = SpektrumOnSurface)
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Hero card ────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                ) {
                    SpectrumBackground(accent = SpektrumAccent, modifier = Modifier.matchParentSize())
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SpektrumAccentTint)
                                    .border(1.dp, SpektrumAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "TA2",
                                    fontFamily = MonoFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = SpektrumAccent,
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ÇAĞRI · TA2/CALL · A SINIFI ADAYI",
                                    style = EyebrowMono,
                                    color = SpektrumMuted,
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = "Seviye $seviye · $seviyeIsim",
                                    fontFamily = SansFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.3).sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            LinearProgressIndicator(
                                progress = { xpYuzde.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SpektrumAccent,
                                trackColor = MaterialTheme.colorScheme.outline,
                            )
                            Text(
                                text = "${kullanici.xp}/${sonrakiXP} XP",
                                style = MutedMono,
                                color = SpektrumMuted,
                            )
                        }
                    }
                }
            }

            // ── 3 stat chips ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProfilStatKutu(
                        etiket = "STREAK",
                        deger  = "${kullanici.streak}",
                        renk   = SpektrumStreak,
                        modifier = Modifier.weight(1f),
                    )
                    ProfilStatKutu(
                        etiket = "TAM. DERS",
                        deger  = "${kullanici.tamamlananDersler}",
                        renk   = Success,
                        modifier = Modifier.weight(1f),
                    )
                    ProfilStatKutu(
                        etiket = "ROZET",
                        deger  = "${kazanilanRozetler.size}/${rozetTanimlari.size}",
                        renk   = SpektrumAccent,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Rozetler ──────────────────────────────────────────────
            item {
                Text(
                    text = "ROZETLER · ${kazanilanRozetler.size}/${rozetTanimlari.size}",
                    style = MutedMono,
                    color = SpektrumMuted,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rozetTanimlari.chunked(3).forEach { satir ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            satir.forEach { rozet ->
                                val kazanildi = rozet.id in kazanilanRozetler
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(
                                            1.dp,
                                            if (kazanildi) SpektrumAccent else MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(12.dp),
                                        )
                                        .alpha(if (kazanildi) 1f else 0.55f)
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (kazanildi) SpektrumAccent
                                                else MaterialTheme.colorScheme.outline
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = rozet.ikon,
                                            fontFamily = MonoFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.5.sp,
                                            color = if (kazanildi) SpektrumBg
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = rozet.isim,
                                        fontFamily = SansFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                    )
                                    Text(
                                        text = rozet.aciklama,
                                        fontFamily = MonoFamily,
                                        fontSize = 9.sp,
                                        color = SpektrumMuted,
                                        maxLines = 2,
                                        lineHeight = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                }
                            }
                            // Fill empty spots in last row
                            repeat(3 - satir.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // ── Bölüm İlerlemesi ──────────────────────────────────────
            item {
                Text(
                    text = "BÖLÜM İLERLEMESİ",
                    style = MutedMono,
                    color = SpektrumMuted,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Column {
                        uiState.curriculum.bolumler.forEachIndexed { i, bolum ->
                            val yuzde = GamificationEngine.getBolumIlerlemeYuzdesi(ilerleme, bolum.id)
                            val renk  = BolumColors.fromId(bolum.id)
                            val no    = bolum.siralama.toString().padStart(2, '0')
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = "B$no",
                                    fontFamily = MonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = renk,
                                    modifier = Modifier.width(32.dp),
                                )
                                Text(
                                    text = bolum.baslik,
                                    fontFamily = SansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                )
                                LinearProgressIndicator(
                                    progress = { yuzde / 100f },
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = renk,
                                    trackColor = MaterialTheme.colorScheme.outline,
                                )
                                Text(
                                    text = "$yuzde%",
                                    fontFamily = MonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SpektrumMuted,
                                    modifier = Modifier.width(32.dp),
                                )
                            }
                            if (i < uiState.curriculum.bolumler.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    thickness = 0.5.dp,
                                )
                            }
                        }
                    }
                }
            }

            // ── Öğrenme Durumu ──────────────────────────────────────
            item {
                val kartlar    = ilerleme.kartlar
                val ogrendim   = kartlar.count { it.value.durum == "ogrendim" }
                val ogreniyor  = kartlar.count { it.value.durum == "ogreniyor" }
                val ogrenmedim = kartlar.count { it.value.durum == "ogrenmedim" }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                ) {
                    Text(
                        text = "ÖĞRENME DURUMU",
                        style = EyebrowMono.copy(color = SpektrumAccent),
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OgrenmeKart("$ogrendim",   "Öğrendim",   Success,       Modifier.weight(1f))
                        OgrenmeKart("$ogreniyor",  "Öğreniyorum", Warn,         Modifier.weight(1f))
                        OgrenmeKart("$ogrenmedim", "Öğrenemedim", SpektrumMuted, Modifier.weight(1f))
                    }
                }
            }

            // ── Veri Yönetimi ────────────────────────────────────────
            item {
                var resetDialogAcik by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("VERİ YÖNETİMİ", style = EyebrowMono.copy(color = SpektrumAccent))
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { exportLauncher.launch("telsiz_okulu_yedek.json") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Veriyi Dışa Aktar", fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, color = SpektrumOnSurface)
                    }
                    Button(
                        onClick = { importLauncher.launch("application/json") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Veriyi İçe Aktar", fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, color = SpektrumOnSurface)
                    }
                    OutlinedButton(
                        onClick = { resetDialogAcik = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                        border = BorderStroke(1.dp, Danger),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Tüm Veriyi Sıfırla", fontFamily = SansFamily, fontWeight = FontWeight.Bold)
                    }

                    if (resetDialogAcik) {
                        AlertDialog(
                            onDismissRequest = { resetDialogAcik = false },
                            title = { Text("İlerlemeyi Sıfırla") },
                            text = { Text("Bu işlem geri alınamaz. Tüm ilerlemeniz silinecek.") },
                            confirmButton = {
                                TextButton(onClick = { viewModel.veriSifirla(); resetDialogAcik = false }) {
                                    Text("Sıfırla", color = Danger)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { resetDialogAcik = false }) { Text("İptal") }
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                    }
                }
            }

            // ── Geliştirici Araçları ──────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("GELİŞTİRİCİ ARAÇLARI", style = EyebrowMono.copy(color = Warn))
                    Text(
                        text = "Test amaçlı — kilitleri açar, ilerlemeyi değiştirir.",
                        style = BodySmall,
                        color = SpektrumMuted,
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.devTumKilitleriAc() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Tüm Kilitleri Aç", fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, color = SpektrumOnSurface)
                    }
                    Button(
                        onClick = { viewModel.devXpEkle() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("+500 XP", fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, color = SpektrumOnSurface)
                    }
                    Button(
                        onClick = { viewModel.devOnboardingSifirla() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text("Öğreticiyi Tekrar Göster", fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, color = SpektrumOnSurface)
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ProfilStatKutu(etiket: String, deger: String, renk: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(renk.copy(alpha = 0.086f))
            .border(1.dp, renk.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = deger,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = renk,
        )
        Text(
            text = etiket,
            style = MutedMono.copy(fontSize = 9.sp),
            color = SpektrumMuted,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

@Composable
private fun OgrenmeKart(deger: String, etiket: String, renk: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(renk.copy(alpha = 0.086f))
            .border(1.dp, renk.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = deger,
            fontFamily = MonoFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = renk,
        )
        Text(
            text = etiket,
            style = BodySmall,
            color = SpektrumMuted,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
