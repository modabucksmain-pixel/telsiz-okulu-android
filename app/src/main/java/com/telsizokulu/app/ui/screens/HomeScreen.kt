package com.telsizokulu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.data.model.Bolum
import com.telsizokulu.app.data.model.KullaniciIlerleme
import com.telsizokulu.app.engine.GamificationEngine
import com.telsizokulu.app.ui.theme.*
import com.telsizokulu.app.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBolumClick: (String) -> Unit,
    onSinavMenuClick: () -> Unit,
    onProfilClick: () -> Unit,
    onKutuphaneClick: () -> Unit,
    onPratikClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val ilerleme = uiState.ilerleme
    val kullanici = ilerleme.kullanici

    val seviye = remember(kullanici.xp) {
        val s = when {
            kullanici.xp >= 5000 -> 7; kullanici.xp >= 3500 -> 6
            kullanici.xp >= 2000 -> 5; kullanici.xp >= 1000 -> 4
            kullanici.xp >= 500 -> 3; kullanici.xp >= 200 -> 2; else -> 1
        }
        s
    }
    val seviyeIsim = GamificationEngine.SEVIYE_ISIMLERI[seviye] ?: "Acemi Telsizci"
    val sonrakiXP = when(seviye) {
        1->200; 2->500; 3->1000; 4->2000; 5->3500; 6->5000; else->5000
    }
    val mevcutSeviyeXP = when(seviye) {
        1->0; 2->200; 3->500; 4->1000; 5->2000; 6->3500; else->5000
    }
    val xpYuzde = if (sonrakiXP == mevcutSeviyeXP) 1f
    else (kullanici.xp - mevcutSeviyeXP).toFloat() / (sonrakiXP - mevcutSeviyeXP)

    // Zayıflık kartları sayısı
    val zayifKonuSayisi = ilerleme.kartlar.count { (_, k) -> k.yanlis > 0 && k.durum != "ogrendim" }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Ana Sayfa") },
                    label = { Text("Ana Sayfa", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Blue60,
                        selectedTextColor = Blue60,
                        indicatorColor = Blue60.copy(alpha = 0.15f),
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onKutuphaneClick,
                    icon = { Icon(Icons.Filled.MenuBook, contentDescription = "Dersler") },
                    label = { Text("Dersler", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Blue60,
                        selectedTextColor = Blue60,
                        indicatorColor = Blue60.copy(alpha = 0.15f),
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSinavMenuClick,
                    icon = { Icon(Icons.Filled.Quiz, contentDescription = "Sınav") },
                    label = { Text("Sınav", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Blue60,
                        selectedTextColor = Blue60,
                        indicatorColor = Blue60.copy(alpha = 0.15f),
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfilClick,
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profil") },
                    label = { Text("Profil", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Blue60,
                        selectedTextColor = Blue60,
                        indicatorColor = Blue60.copy(alpha = 0.15f),
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    )
                )
            }
        }
    ) { paddingValues ->
        if (uiState.yukleniyor) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue60)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // ── Hero Banner ────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Slate900, Slate950)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Text(
                            text = "📻 Telsiz Okulu",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = SlateWhite
                            )
                        )
                        Text(
                            text = "TRAC sınavına hazırlanmak için en etkili yol",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate400,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    val ilkBolum = uiState.curriculum.bolumler.firstOrNull()?.id ?: "bolum_1"
                                    onBolumClick(ilkBolum)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Blue60),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🚀 Öğrenmeye Başla", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onSinavMenuClick,
                                border = BorderStroke(1.5.dp, Slate700),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📝 Deneme Sınavı", color = SlateWhite, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ── İstatistik Kartları ────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatKart(modifier = Modifier.weight(1f), ikon = "⭐", deger = kullanici.xp.toString(), baslik = "XP", renk = GoldXP)
                    StatKart(modifier = Modifier.weight(1f), ikon = "🔥", deger = kullanici.streak.toString(), baslik = "Streak", renk = OrangeStreak)
                    StatKart(modifier = Modifier.weight(1f), ikon = "📚", deger = kullanici.tamamlananDersler.toString(), baslik = "Ders", renk = GreenSuccess)
                    StatKart(modifier = Modifier.weight(1f), ikon = "🏆", deger = kullanici.rozetler.size.toString(), baslik = "Rozet", renk = Purple60)
                }
            }

            // ── XP Seviye Barı ────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sv.$seviye $seviyeIsim",
                                style = MaterialTheme.typography.labelLarge,
                                color = Blue60
                            )
                            Text(
                                text = "${kullanici.xp} / $sonrakiXP XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { xpYuzde.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Blue60,
                            trackColor = Slate800
                        )
                    }
                }
            }

            // ── Zayıflık Pratiği Butonu ────────────────────────────
            if (zayifKonuSayisi > 0) {
                item {
                    Button(
                        onClick = onPratikClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "🎯 Öğrenemediklerimi Tekrar Et ($zayifKonuSayisi konu)",
                            color = Slate950,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Öğrenme Yolculuğu Başlığı ─────────────────────────
            item {
                Text(
                    text = "🗺️ Öğrenme Yolculuğun",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SlateWhite,
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 4.dp)
                )
                Text(
                    text = "Bölümleri sırayla tamamlayarak uzman ol!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400,
                    modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
                )
            }

            // ── Bölüm Kartları ────────────────────────────────────
            items(uiState.curriculum.bolumler) { bolum ->
                val bolumDurum = ilerleme.bolumler[bolum.id]?.durum ?: "kilitli"
                val ilerlemeYuzde = uiState.bolumIlerlemeleri[bolum.id] ?: 0
                val kilitli = bolumDurum == "kilitli"

                BolumKart(
                    bolum = bolum,
                    durum = bolumDurum,
                    ilerlemeYuzde = ilerlemeYuzde,
                    kilitli = kilitli,
                    onClick = { if (!kilitli) onBolumClick(bolum.id) }
                )
            }

            // ── Günlük Hedef ──────────────────────────────────────
            item {
                val gunlukDers = kullanici.gunlukDers
                val hedef = 3
                val yuzde = (gunlukDers.toFloat() / hedef).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("🎯 Günlük Hedef", style = MaterialTheme.typography.titleMedium, color = SlateWhite)
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { yuzde },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = AmberWarning,
                            trackColor = Slate800
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Bugün $gunlukDers/$hedef ders tamamlandı",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatKart(
    modifier: Modifier = Modifier,
    ikon: String,
    deger: String,
    baslik: String,
    renk: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = renk.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, renk.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = ikon, fontSize = 30.sp)
            Text(
                text = deger,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = renk
            )
            Text(text = baslik, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = renk.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun BolumKart(
    bolum: Bolum,
    durum: String,
    ilerlemeYuzde: Int,
    kilitli: Boolean,
    onClick: () -> Unit
) {
    val renk = try {
        Color(android.graphics.Color.parseColor(bolum.renk))
    } catch (e: Exception) { Blue60 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(if (!kilitli) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (kilitli) Slate900.copy(alpha = 0.5f) else Slate900
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.heightIn(min = 100.dp)) {
            // Sol renk çubuğu
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(
                        if (kilitli) Slate700 else renk,
                        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // İkon kutusu
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (kilitli) Slate800 else renk.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (kilitli) "🔒" else bolum.ikon, fontSize = 30.sp)
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bölüm ${bolum.siralama}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (kilitli) Slate700 else renk,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = bolum.baslik,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (kilitli) Slate700 else SlateWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = bolum.aciklama,
                        fontSize = 13.sp,
                        color = Slate400,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (!kilitli) {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { ilerlemeYuzde / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = renk,
                                trackColor = Slate800
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "$ilerlemeYuzde%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = renk
                            )
                        }
                    }
                }

                if (!kilitli) {
                    Text(
                        text = when (durum) {
                            "tamamlandi"   -> "✅"
                            "devam_ediyor" -> "▶️"
                            else           -> ""
                        },
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
