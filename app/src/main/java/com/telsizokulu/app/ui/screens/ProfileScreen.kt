package com.telsizokulu.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.engine.GamificationEngine
import com.telsizokulu.app.ui.theme.*
import com.telsizokulu.app.ui.viewmodel.ProfileViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onGeri: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportJson { json ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(json.toByteArray())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val json = input.bufferedReader().use { it.readText() }
                    viewModel.importJson(json)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val ilerleme = uiState.ilerleme
    val kullanici = ilerleme.kullanici

    val seviye = when {
        kullanici.xp >= 5000 -> 7; kullanici.xp >= 3500 -> 6
        kullanici.xp >= 2000 -> 5; kullanici.xp >= 1000 -> 4
        kullanici.xp >= 500  -> 3; kullanici.xp >= 200  -> 2; else -> 1
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = SlateWhite,
                    navigationIconContentColor = SlateWhite
                )
            )
        },
        containerColor = Slate950
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Profil Başlığı ──────────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Blue60.copy(alpha = 0.15f))
                                .border(3.dp, Blue60.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📻", fontSize = 44.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = seviyeIsim,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = SlateWhite
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Blue60.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = "Seviye $seviye",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue60,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // XP progress bar — full width inside hero card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sv.$seviye", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue60)
                            Spacer(Modifier.width(8.dp))
                            LinearProgressIndicator(
                                progress = { xpYuzde.coerceIn(0f, 1f) },
                                modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(6.dp)),
                                color = Blue60,
                                trackColor = Slate800
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sv.${seviye + 1}", fontSize = 12.sp, color = Slate400)
                        }
                        Text(
                            "${kullanici.xp} / $sonrakiXP XP",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(Modifier.height(20.dp))

                        // Stat row — bigger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfilStatItem("${kullanici.xp}", "XP", GoldXP)
                            ProfilStatItem("${kullanici.streak}", "🔥 Gün", OrangeStreak)
                            ProfilStatItem("${kullanici.tamamlananDersler}", "Ders", GreenSuccess)
                            ProfilStatItem("${kullanici.rozetler.size}", "Rozet", Purple60)
                        }
                    }
                }
            }

            // ── Bölüm İlerlemeleri ──────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "📊 Bölüm İlerlemeleri",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SlateWhite
                        )
                        Spacer(Modifier.height(16.dp))
                        uiState.curriculum.bolumler.forEach { bolum ->
                            val yuzde = GamificationEngine.getBolumIlerlemeYuzdesi(ilerleme, bolum.id)
                            val renk = try { Color(android.graphics.Color.parseColor(bolum.renk)) } catch (e: Exception) { Blue60 }
                            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(bolum.ikon, fontSize = 18.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        bolum.baslik,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = SlateWhite,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "$yuzde%",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = renk
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { yuzde / 100f },
                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                    color = renk,
                                    trackColor = Slate800
                                )
                            }
                        }
                    }
                }
            }

            // ── Öğrenme Durumu ──────────────────────────────────
            item {
                val kartlar = ilerleme.kartlar
                val ogrendim = kartlar.count { it.value.durum == "ogrendim" }
                val ogreniyor = kartlar.count { it.value.durum == "ogreniyor" }
                val ogrenmedim = kartlar.count { it.value.durum == "ogrenmedim" }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "📚 Öğrenme Durumu",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SlateWhite
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OgrenmeStatKart("✅", "$ogrendim", "Öğrendim", GreenSuccess, Modifier.weight(1f))
                            OgrenmeStatKart("📖", "$ogreniyor", "Öğreniyorum", AmberWarning, Modifier.weight(1f))
                            OgrenmeStatKart("❓", "$ogrenmedim", "Öğrenemedim", Slate400, Modifier.weight(1f))
                        }
                    }
                }
            }

            // ── Rozet Vitrini ───────────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "🏆 Rozet Vitrini",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SlateWhite
                        )
                        Text(
                            "${kullanici.rozetler.size} / ${GamificationEngine.ROZET_BILGILERI.size} kazanıldı",
                            fontSize = 12.sp,
                            color = Slate400,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                        GamificationEngine.ROZET_BILGILERI.entries.chunked(3).forEach { grup ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                grup.forEach { (rozetId, bilgi) ->
                                    val kazanildi = rozetId in kullanici.rozetler
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (kazanildi) GoldXP.copy(alpha = 0.12f) else Slate800.copy(alpha = 0.4f)
                                            )
                                            .border(
                                                1.dp,
                                                if (kazanildi) GoldXP.copy(alpha = 0.3f) else Slate700.copy(alpha = 0.3f),
                                                RoundedCornerShape(14.dp)
                                            )
                                            .alpha(if (kazanildi) 1f else 0.4f)
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(bilgi.first, fontSize = 28.sp)
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            GamificationEngine.getRozetIsim(rozetId),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = if (kazanildi) GoldXP else Slate700,
                                            maxLines = 2
                                        )
                                    }
                                }
                                repeat(3 - grup.size) { Spacer(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }

            // ── Veri Yönetimi ──────────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "💾 Veri Yönetimi",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SlateWhite
                        )
                        Spacer(Modifier.height(14.dp))
                        var sifirlamaOnayDialogAcik by remember { mutableStateOf(false) }

                        Button(
                            onClick = { exportLauncher.launch("telsiz_okulu_yedek.json") },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("📤 Veriyi Dışa Aktar", color = SlateWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = { importLauncher.launch("application/json") },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("📥 Veriyi İçe Aktar", color = SlateWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                        Spacer(Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { sifirlamaOnayDialogAcik = true },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = RedError,
                                disabledContainerColor = Color.Transparent,
                                disabledContentColor = Slate700
                            ),
                            border = BorderStroke(1.5.dp, RedError),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("🗑️ Tüm Veriyi Sıfırla", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        if (sifirlamaOnayDialogAcik) {
                            AlertDialog(
                                onDismissRequest = { sifirlamaOnayDialogAcik = false },
                                title = { Text("Tüm İlerlemeyi Sıfırla") },
                                text = { Text("Bu işlem geri alınamaz! Tüm ilerlemeniz silinecek.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.veriSifirla()
                                            sifirlamaOnayDialogAcik = false
                                        }
                                    ) { Text("Sıfırla", color = RedError) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { sifirlamaOnayDialogAcik = false }) {
                                        Text("İptal")
                                    }
                                },
                                containerColor = Slate900
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilStatItem(deger: String, baslik: String, renk: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = deger,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = renk
        )
        Text(text = baslik, style = MaterialTheme.typography.labelSmall, color = Slate400)
    }
}

@Composable
private fun OgrenmeStatKart(ikon: String, deger: String, baslik: String, renk: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(renk.copy(alpha = 0.1f))
            .border(1.dp, renk.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(ikon, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            deger,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = renk
        )
        Text(
            baslik,
            fontSize = 11.sp,
            color = Slate400,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
