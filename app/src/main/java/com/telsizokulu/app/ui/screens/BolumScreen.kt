package com.telsizokulu.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telsizokulu.app.data.model.AltBolum
import com.telsizokulu.app.data.model.Bolum
import com.telsizokulu.app.data.model.Ders
import com.telsizokulu.app.ui.theme.*
import com.telsizokulu.app.ui.viewmodel.BolumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolumScreen(
    viewModel: BolumViewModel,
    onGeri: () -> Unit,
    onDersClick: (altBolumId: String, dersNo: Int) -> Unit,
    onAltBolumSinavClick: (altBolumId: String) -> Unit,
    onBolumSinavClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val bolum = uiState.bolum
    val ilerleme = uiState.ilerleme

    if (uiState.yukleniyor || bolum == null) {
        Box(Modifier.fillMaxSize().background(SpektrumBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SpektrumAccent)
        }
        return
    }

    val renk = try {
        Color(android.graphics.Color.parseColor(bolum.renk))
    } catch (e: Exception) { SpektrumAccent }

    val bolumDurum = ilerleme.bolumler[bolum.id]?.durum ?: "kilitli"
    val gecmeSinaviDurum = ilerleme.bolumler[bolum.id]?.gecmeSinavi?.durum ?: "kilitli"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bölüm ${bolum.siralama}: ${bolum.baslik}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SpektrumSurface,
                    titleContentColor = SpektrumOnSurface,
                    navigationIconContentColor = SpektrumOnSurface
                )
            )
        },
        containerColor = SpektrumBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bölüm başlığı — left color bar + big icon
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SpektrumSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Left color bar
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                                .background(renk)
                                .defaultMinSize(minHeight = 120.dp)
                        )
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(renk.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(bolum.ikon, fontSize = 32.sp)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "BÖLÜM ${bolum.siralama}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = renk,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = bolum.baslik,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = SpektrumOnSurface
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = bolum.aciklama,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SpektrumMuted
                            )
                        }
                    }
                }
            }

            // Alt bölümler
            itemsIndexed(bolum.altBolumler) { idx, altBolum ->
                val bolumBilgi = ilerleme.bolumler[bolum.id]
                val altBolumIlerleme = bolumBilgi?.altBolumler?.get(altBolum.id)
                val tamamlananDersler = altBolumIlerleme?.tamamlananDersler ?: emptyList()

                // Kilit, saklanan duruma değil türetilmiş kurala bağlı:
                // ilk alt bölüm her zaman açık; sonrakiler ancak önceki alt bölüm sınavı geçilince açılır.
                val buTamam = altBolumIlerleme?.durum == "tamamlandi"
                val oncekiId = bolum.altBolumler.getOrNull(idx - 1)?.id
                val oncekiTamam = idx == 0 ||
                    bolumBilgi?.altBolumler?.get(oncekiId)?.durum == "tamamlandi"
                val effektifDurum = when {
                    buTamam      -> "tamamlandi"
                    oncekiTamam  -> "devam_ediyor"
                    else         -> "kilitli"
                }

                AltBolumKart(
                    bolum = bolum,
                    altBolum = altBolum,
                    durum = effektifDurum,
                    tamamlananDersler = tamamlananDersler,
                    renk = renk,
                    onDersClick = { dersNo -> onDersClick(altBolum.id, dersNo) },
                    onSinavClick = { onAltBolumSinavClick(altBolum.id) }
                )
            }

            // Bölüm geçme sınavı
            item {
                val sinavAcik = gecmeSinaviDurum == "acik" || gecmeSinaviDurum == "tamamlandi"
                val sinavTamamlandi = gecmeSinaviDurum == "tamamlandi"

                Button(
                    onClick = { if (sinavAcik) onBolumSinavClick() },
                    modifier = Modifier.fillMaxWidth().height(62.dp),
                    enabled = sinavAcik,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sinavTamamlandi) Success else renk,
                        disabledContainerColor = SpektrumOutline2
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = when {
                            sinavTamamlandi -> "✅ Bölüm Tamamlandı!"
                            sinavAcik -> "🎯 Bölüm ${bolum.siralama} Geçme Sınavı"
                            else -> "🔒 Tüm dersler tamamlandığında açılacak"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp,
                        color = if (sinavAcik) SpektrumOnSurface else SpektrumOutline
                    )
                }

                if (!sinavAcik) {
                    Text(
                        text = "Tüm alt bölümleri tamamlayınca aktif olur • %${bolum.gecmeSinavi.gecmePuani} başarı gerekli",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpektrumMuted,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AltBolumKart(
    bolum: Bolum,
    altBolum: AltBolum,
    durum: String,
    tamamlananDersler: List<Int>,
    renk: Color,
    onDersClick: (Int) -> Unit,
    onSinavClick: () -> Unit
) {
    val kilitli = durum == "kilitli"
    val tamamlandi = durum == "tamamlandi"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (kilitli) SpektrumSurface.copy(alpha = 0.5f) else SpektrumSurface
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().alpha(if (kilitli) 0.55f else 1f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Başlık + badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = altBolum.baslik,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (kilitli) SpektrumOutline else SpektrumOnSurface
                    )
                    Text(
                        text = altBolum.aciklama,
                        style = MaterialTheme.typography.bodySmall,
                        color = SpektrumMuted,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        tamamlandi -> Success.copy(alpha = 0.2f)
                        kilitli -> SpektrumOutline2
                        else -> renk.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when {
                            tamamlandi -> "✅"
                            kilitli -> "🔒"
                            else -> "📖"
                        },
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Ders listesi — bigger nodes
            altBolum.dersler.forEach { ders ->
                val dersTamamlandi = ders.no in tamamlananDersler
                val maxTamamlanan = tamamlananDersler.maxOrNull() ?: 0
                val dersAcik = !kilitli && (ders.no <= maxTamamlanan + 1)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (dersAcik) Modifier.clickable { onDersClick(ders.no) }
                            else Modifier
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lesson circle node
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    dersTamamlandi -> Success
                                    dersAcik -> renk
                                    else -> SpektrumOutline2
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dersTamamlandi) {
                            Text("✓", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SpektrumOnSurface)
                        } else if (!dersAcik) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = SpektrumOutline, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                text = "${ders.no}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = SpektrumOnSurface
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ders.baslik,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = when {
                                dersTamamlandi -> Success
                                dersAcik -> SpektrumOnSurface
                                else -> SpektrumOutline
                            }
                        )
                        if (dersTamamlandi) {
                            Text("Tamamlandı", fontSize = 12.sp, color = Success.copy(alpha = 0.7f))
                        } else if (dersAcik) {
                            Text("Başlamak için dokun", fontSize = 12.sp, color = SpektrumMuted)
                        }
                    }
                }

                if (ders != altBolum.dersler.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        color = SpektrumOutline2.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Alt bölüm sınavı butonu
            val sinavAcik = !kilitli && (tamamlananDersler.size >= altBolum.dersler.size || tamamlandi)
            OutlinedButton(
                onClick = { if (sinavAcik) onSinavClick() },
                enabled = sinavAcik,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, if (tamamlandi) Success else if (sinavAcik) renk else SpektrumOutline2)
            ) {
                Text(
                    text = if (tamamlandi) "✅ Sınav Geçildi" else "🧪 Alt Bölüm Sınavı",
                    color = if (tamamlandi) Success else if (sinavAcik) renk else SpektrumOutline,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
