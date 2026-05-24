package com.telsizokulu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.data.model.IcerikDosyasi
import com.telsizokulu.app.data.model.KullaniciIlerleme
import com.telsizokulu.app.data.repository.CurriculumRepository
import com.telsizokulu.app.data.repository.ProgressRepository
import com.telsizokulu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KutuphaneScreen(
    curriculumRepo: CurriculumRepository,
    progressRepo: ProgressRepository,
    onGeri: () -> Unit
) {
    var kutuphaneVerisi by remember { mutableStateOf<List<Pair<String, IcerikDosyasi>>>(emptyList()) }
    var ilerleme by remember { mutableStateOf(KullaniciIlerleme()) }
    var seciliSekme by remember { mutableStateOf("ogrenmedim") }

    LaunchedEffect(Unit) {
        val dosyalar = listOf(
            "nato.json", "qcodes.json", "elektronik.json",
            "bantlar.json", "prosedurler.json", "sinav_sorulari.json"
        )
        kutuphaneVerisi = dosyalar.mapNotNull { dosya ->
            val icerik = curriculumRepo.loadIcerik(dosya)
            if (icerik.kartlar.isNotEmpty()) Pair(
                icerik.baslik.ifEmpty { dosya.removeSuffix(".json").replaceFirstChar { it.uppercase() } },
                icerik
            ) else null
        }
        ilerleme = progressRepo.getIlerleme()
    }

    val sekmeler = listOf(
        Triple("ogrendim",   "✅ Öğrendim",   GreenSuccess),
        Triple("ogreniyor",  "📖 Öğreniyorum", AmberWarning),
        Triple("ogrenmedim", "❓ Öğrenemedim", Slate400)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ders Notları") },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Sekme satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sekmeler.forEach { (key, label, renk) ->
                    val secili = seciliSekme == key
                    FilterChip(
                        selected = secili,
                        onClick = { seciliSekme = key },
                        label = {
                            Text(
                                label,
                                fontSize = 12.sp,
                                fontWeight = if (secili) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = renk.copy(alpha = 0.2f),
                            selectedLabelColor = renk,
                            containerColor = Slate800,
                            labelColor = Slate400
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = secili,
                            selectedBorderColor = renk.copy(alpha = 0.5f),
                            borderColor = Slate700
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                kutuphaneVerisi.forEach { (baslik, icerik) ->
                    val filtreliKartlar = icerik.kartlar.filter { kart ->
                        when (seciliSekme) {
                            "ogrendim"  -> ilerleme.kartlar[kart.id]?.durum == "ogrendim"
                            "ogreniyor" -> ilerleme.kartlar[kart.id]?.durum == "ogreniyor"
                            else        -> {
                                val d = ilerleme.kartlar[kart.id]?.durum
                                d == "ogrenmedim" || d == null
                            }
                        }
                    }

                    if (filtreliKartlar.isNotEmpty()) {
                        item(key = baslik) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = baslik,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SlateWhite
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate800)

                                    filtreliKartlar.forEach { kart ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            if (!kart.ikon.isNullOrEmpty()) {
                                                Text(kart.ikon, fontSize = 24.sp)
                                            } else {
                                                Text("📌", fontSize = 24.sp)
                                            }
                                            Column {
                                                Text(
                                                    text = kart.on ?: "",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                                    color = Blue60
                                                )
                                                Text(
                                                    text = kart.arka ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Slate400,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = Slate800.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    if (kutuphaneVerisi.isNotEmpty() && kutuphaneVerisi.all { (_, icerik) ->
                            icerik.kartlar.none { kart ->
                                when (seciliSekme) {
                                    "ogrendim"  -> ilerleme.kartlar[kart.id]?.durum == "ogrendim"
                                    "ogreniyor" -> ilerleme.kartlar[kart.id]?.durum == "ogreniyor"
                                    else        -> ilerleme.kartlar[kart.id]?.durum.let { it == "ogrenmedim" || it == null }
                                }
                            }
                        }
                    ) {
                        val mesaj = when (seciliSekme) {
                            "ogrendim"  -> "Henüz öğrendiğin kart yok.\nDersleri tamamladıkça burası dolacak! 🎯"
                            "ogreniyor" -> "Öğrenme sürecinde kart yok.\nDerslere gir ve alıştırma yap! 📖"
                            else        -> "Tüm kartları öğrendin!\nMükemmel! ✅"
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mesaj, color = Slate400, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
