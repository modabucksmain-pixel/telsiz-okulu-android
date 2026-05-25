package com.telsizokulu.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.telsizokulu.app.data.model.Egzersiz
import com.telsizokulu.app.data.model.Kart
import com.telsizokulu.app.data.model.DiyalogSatiri
import com.telsizokulu.app.data.model.HizliTurSoruTanim
import com.telsizokulu.app.engine.GamificationEngine
import com.telsizokulu.app.ui.components.BadgeIcon
import com.telsizokulu.app.ui.components.NatoMonogram
import com.telsizokulu.app.ui.theme.*
import com.telsizokulu.app.ui.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    viewModel: ExerciseViewModel,
    onGeri: () -> Unit,
    onTamamlandi: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val oturum = uiState.oturum

    // Tamamlandıysa özet ekranını göster
    if (oturum.tamamlandi) {
        OzetEkrani(
            viewModel = viewModel,
            onTamamlandi = onTamamlandi
        )
        return
    }

    if (uiState.yukleniyor) {
        Box(Modifier.fillMaxSize().background(Slate950), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Blue60)
        }
        return
    }

    // Bilgi Kartları (Teori) Modu devredeyse teori ekranını render et
    if (uiState.teoriModu && uiState.teoriKartlari.isNotEmpty()) {
        TeoriEkrani(
            viewModel = viewModel,
            teoriKartlari = uiState.teoriKartlari,
            mevcutTeoriIndex = uiState.mevcutTeoriIndex,
            kartCevrildi = uiState.kartCevrildi,
            onGeri = onGeri,
            onTeoriCevir = viewModel::teoriCevir,
            onTeoriSonraki = viewModel::teoriSonraki,
            onTeoriGec = viewModel::teoriGec,
            onSeslendir = viewModel::seslendirMevcutTeori
        )
        return
    }

    val egzersiz = oturum.mevcutEgzersiz
    if (egzersiz == null) {
        Box(Modifier.fillMaxSize().background(Slate950), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())
            ) {
                Text("🎓", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text("Bu ders için içerik bulunamadı.", color = Slate400, textAlign = TextAlign.Center)
                if (uiState.hata != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "HATA:\n${uiState.hata}",
                        color = RedError,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "mod=${uiState.mod} bolum=${uiState.bolumId} alt=${uiState.altBolumId} ders=${uiState.dersNo}\nkart=${uiState.icerik.kartlar.size} egz=${uiState.oturum.egzersizler.size}",
                        color = Slate400,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(24.dp))
                Button(onClick = onGeri) { Text("Geri Dön") }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // ── Üst Bar (İlerleme + Kapat) ─────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onGeri) {
                Icon(Icons.Filled.Close, contentDescription = "Çık", tint = Slate400)
            }
            Spacer(Modifier.width(8.dp))
            val animProgress by animateFloatAsState(
                targetValue = oturum.ilerlemeYuzdesi,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                label = "progress"
            )
            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp)),
                color = GreenSuccess,
                trackColor = Slate800
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${oturum.mevcutIndex + 1}/${oturum.egzersizler.size}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Slate400
            )
        }

        // ── XP Animasyonu ─────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.showXpAnimation && uiState.xpKazanildi > 0,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GoldXP.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+${uiState.xpKazanildi} XP",
                        color = GoldXP,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
        LaunchedEffect(uiState.showXpAnimation) {
            if (uiState.showXpAnimation) {
                kotlinx.coroutines.delay(1200)
                viewModel.xpAnimasyonGizle()
            }
        }

        // ── Egzersiz Alanı ─────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            EgzersizKarti(
                egzersiz = egzersiz,
                geribildirımGoruntu = oturum.geribildirımGoruntu,
                sonCevapDogru = oturum.sonCevapDogru,
                onCevapVer = viewModel::cevapVer,
                onDevamEt = viewModel::devamEt
            )
        }
    }
}

@Composable
private fun EgzersizKarti(
    egzersiz: Egzersiz,
    geribildirımGoruntu: Boolean,
    sonCevapDogru: Boolean?,
    onCevapVer: (String) -> Unit,
    onDevamEt: () -> Unit
) {
    var mevcutCevap by remember(egzersiz.id) { mutableStateOf("") }
    var cevapGonderildi by remember(egzersiz.id) { mutableStateOf(false) }

    var girisGorunur by remember(egzersiz.id) { mutableStateOf(false) }
    LaunchedEffect(egzersiz.id) { girisGorunur = true }
    val girisAlpha by animateFloatAsState(
        targetValue = if (girisGorunur) 1f else 0f,
        animationSpec = tween(360), label = "girisAlpha"
    )
    val girisKayma by animateFloatAsState(
        targetValue = if (girisGorunur) 0f else 48f,
        animationSpec = tween(360, easing = FastOutSlowInEasing), label = "girisKayma"
    )

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = girisAlpha; translationY = girisKayma }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = if (geribildirımGoruntu) 220.dp else 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Tip Etiketi
        Text(
            text = getTipEtiketi(egzersiz.tip),
            color = Blue60,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Konuşma Balonları (Diyalog)
        if (egzersiz.diyalog != null && egzersiz.diyalog.isNotEmpty()) {
            DiyalogKutusu(diyalog = egzersiz.diyalog)
            Spacer(Modifier.height(16.dp))
        }

        // Soru Metni
        Text(
            text = egzersiz.soru.ifEmpty { egzersiz.metin },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp,
            color = SlateWhite,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        // Cevap seçenekleri veya özel soru tipleri
        when (egzersiz.tip) {
            "coktan_secmeli", "anlam_bul", "kodu_bul", "dinle_sec", "cumle_dinle", "diyalog_tamamla", "diyalog", "durum_sec" -> {
                val numLabels = listOf("A", "B", "C", "D")
                egzersiz.secenekler.forEachIndexed { i, secenek ->
                    val secildi = mevcutCevap == secenek
                    val dogru = geribildirımGoruntu && secenek == egzersiz.cevap
                    val yanlis = geribildirımGoruntu && secildi && !dogru

                    val vurguRenk = when {
                        dogru -> GreenSuccess
                        yanlis -> RedError
                        secildi && !geribildirımGoruntu -> Blue60
                        else -> null
                    }
                    Button(
                        onClick = {
                            if (!cevapGonderildi) {
                                mevcutCevap = secenek
                                cevapGonderildi = true
                                onCevapVer(secenek)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .then(
                                if (vurguRenk != null)
                                    Modifier.neonGlow(vurguRenk, cornerRadius = 18.dp, elevation = 10.dp, borderWidth = 2.dp)
                                else Modifier
                            )
                            .heightIn(min = 72.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                dogru -> GreenSuccess.copy(alpha = 0.25f)
                                yanlis -> RedError.copy(alpha = 0.25f)
                                secildi && !geribildirımGoruntu -> Blue60.copy(alpha = 0.2f)
                                else -> Slate900
                            }
                        ),
                        border = if (vurguRenk == null) BorderStroke(2.dp, Slate700) else null,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            dogru -> GreenSuccess.copy(alpha = 0.3f)
                                            yanlis -> RedError.copy(alpha = 0.3f)
                                            secildi && !geribildirımGoruntu -> Blue60.copy(alpha = 0.3f)
                                            else -> Slate800
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = numLabels.getOrNull(i) ?: "•",
                                    color = when {
                                        dogru -> GreenSuccess
                                        yanlis -> RedError
                                        secildi && !geribildirımGoruntu -> Blue60
                                        else -> Slate400
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = secenek,
                                color = when {
                                    dogru -> GreenSuccess
                                    yanlis -> RedError
                                    secildi && !geribildirımGoruntu -> Blue60
                                    else -> SlateWhite
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }

            "dogru_yanlis" -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Doğru" to "dogru", "Yanlış" to "yanlis").forEach { (label, value) ->
                        val secildi = mevcutCevap == value
                        val dogruCevap = egzersiz.cevap
                        val expected = if (dogruCevap.trim().equals("Doğru", ignoreCase = true)) "dogru" else "yanlis"
                        val dogru = geribildirımGoruntu && value == expected
                        val yanlis = geribildirımGoruntu && secildi && !dogru

                        Button(
                            onClick = {
                                if (!cevapGonderildi) {
                                    mevcutCevap = value
                                    cevapGonderildi = true
                                    onCevapVer(value)
                                }
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 80.dp),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    dogru -> GreenSuccess.copy(alpha = 0.2f)
                                    yanlis -> RedError.copy(alpha = 0.2f)
                                    secildi && !geribildirımGoruntu -> Blue60.copy(alpha = 0.2f)
                                    else -> Slate900
                                }
                            ),
                            border = BorderStroke(2.dp, when {
                                dogru -> GreenSuccess
                                yanlis -> RedError
                                secildi && !geribildirımGoruntu -> Blue60
                                else -> Slate700
                            }),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (value == "dogru") "✅" else "❌",
                                    fontSize = 26.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    color = when { dogru -> GreenSuccess; yanlis -> RedError; else -> SlateWhite },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            "bosluk_doldur", "sayisal", "kelime_hece", "cumle_tamamla", "formul_tamamla", "kodu_yaz", "harfi_yaz", "hesapla" -> {
                val unitSuffix = if ((egzersiz.tip == "sayisal" || egzersiz.tip == "hesapla") && egzersiz.birim.isNotEmpty()) " (${egzersiz.birim})" else ""
                OutlinedTextField(
                    value = mevcutCevap,
                    onValueChange = { mevcutCevap = it },
                    label = { Text("Cevabınızı buraya yazın$unitSuffix...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !cevapGonderildi,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue60,
                        unfocusedBorderColor = Slate700,
                        focusedLabelColor = Blue60,
                        cursorColor = Blue60,
                        focusedTextColor = SlateWhite,
                        unfocusedTextColor = SlateWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (!cevapGonderildi && mevcutCevap.isNotBlank()) {
                            cevapGonderildi = true
                            onCevapVer(mevcutCevap.trim())
                        }
                    },
                    enabled = !cevapGonderildi && mevcutCevap.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue60),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("CEVABI GÖNDER  ›", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 1.sp)
                }
            }

            "eslestir", "eslestime" -> {
                EslestirmeSoru(
                    egzersiz = egzersiz,
                    onCevapVer = {
                        mevcutCevap = "dogru"
                        cevapGonderildi = true
                        onCevapVer("dogru")
                    }
                )
            }

            "sirala" -> {
                SıralamaSoru(
                    egzersiz = egzersiz,
                    onCevapVer = { sonuc ->
                        mevcutCevap = sonuc
                        cevapGonderildi = true
                        onCevapVer(sonuc)
                    }
                )
            }

            "hizli_tur" -> {
                HizliTurSoru(
                    egzersiz = egzersiz,
                    onCevapVer = { sonuc ->
                        mevcutCevap = sonuc
                        cevapGonderildi = true
                        onCevapVer(sonuc)
                    }
                )
            }
        }

    }

        // ── Geri Bildirim Bottom Sheet ─────────────────────────
        AnimatedVisibility(
            visible = geribildirımGoruntu,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = if (sonCevapDogru == true) GreenSuccess.copy(alpha = 0.12f) else RedError.copy(alpha = 0.12f),
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (sonCevapDogru == true) "✅  Harika!" else "❌  Yanlış Cevap!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (sonCevapDogru == true) GreenSuccess else RedError
                    )
                    val aciklama = egzersiz.aciklama.ifEmpty {
                        val dogruCevap = egzersiz.cevap
                        if (dogruCevap.isNotEmpty()) "Doğru cevap: $dogruCevap" else ""
                    }
                    if (aciklama.isNotEmpty()) {
                        Text(
                            text = aciklama.replace("<br>", "\n").replace(Regex("<[^>]*>"), ""),
                            fontSize = 15.sp,
                            color = SlateWhite.copy(alpha = 0.85f)
                        )
                    }
                    val feedbackImg = remember(egzersiz.aciklama) { getFeedbackImage(egzersiz.aciklama) }
                    if (feedbackImg != null) {
                        AsyncImage(
                            model = feedbackImg,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate950)
                                .padding(8.dp)
                        )
                    }
                    Button(
                        onClick = onDevamEt,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sonCevapDogru == true) GreenSuccess else Blue60
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "DEVAM ET",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp,
                            color = SlateWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiyalogKutusu(diyalog: List<DiyalogSatiri>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Slate900, shape = RoundedCornerShape(16.dp))
            .border(1.dp, Slate800, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        diyalog.forEach { satir ->
            val isAlici = satir.taraf == "alici"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isAlici) Arrangement.Start else Arrangement.End
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(
                            color = if (isAlici) Slate800 else Blue60.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isAlici) 2.dp else 14.dp,
                                bottomEnd = if (isAlici) 14.dp else 2.dp
                            )
                        )
                        .border(
                            1.dp,
                            color = if (isAlici) Slate700 else Blue60.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isAlici) 2.dp else 14.dp,
                                bottomEnd = if (isAlici) 14.dp else 2.dp
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "📻 ${satir.kisi}:",
                        color = if (isAlici) SlateWhite else Blue60,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = satir.metin,
                        color = SlateWhite,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun TeoriEkrani(
    viewModel: ExerciseViewModel,
    teoriKartlari: List<Kart>,
    mevcutTeoriIndex: Int,
    kartCevrildi: Boolean,
    onGeri: () -> Unit,
    onTeoriCevir: () -> Unit,
    onTeoriSonraki: () -> Unit,
    onTeoriGec: () -> Unit = { viewModel.teoriGec() },
    onSeslendir: () -> Unit = { viewModel.seslendirMevcutTeori() }
) {
    val kart = teoriKartlari.getOrNull(mevcutTeoriIndex) ?: return
    val rotation by animateFloatAsState(
        targetValue = if (kartCevrildi) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "cardFlip"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        // Üst Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onGeri) {
                Icon(Icons.Filled.Close, contentDescription = "Kapat", tint = Slate400)
            }
            Spacer(Modifier.width(8.dp))
            LinearProgressIndicator(
                progress = { if (teoriKartlari.isEmpty()) 0f else mevcutTeoriIndex.toFloat() / teoriKartlari.size },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Blue60,
                trackColor = Slate800
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${mevcutTeoriIndex + 1}/${teoriKartlari.size} Bilgi",
                style = MaterialTheme.typography.labelLarge,
                color = Slate400
            )
        }

        Spacer(Modifier.height(16.dp))

        // 3D Flippable Card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .neonGlow(
                        color = if (kartCevrildi) Purple60 else Blue60,
                        cornerRadius = 24.dp,
                        elevation = 20.dp,
                    )
                    .clickable { onTeoriCevir() }
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 8 * density
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                if (rotation <= 90f) {
                    // Front Face
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Visual — NATO harfleri için özgün monogram, diğerleri için konu görseli
                        if (kart.id.startsWith("nato_") && !kart.on.isNullOrEmpty()) {
                            NatoMonogram(harf = kart.on ?: "", accent = Blue60)
                        } else {
                            val gorsel = getTeoriKartGorselYolu(kart.id, kart.on ?: "", kart.arka ?: "")
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .neonGlow(Blue60, cornerRadius = 20.dp, elevation = 12.dp, glowAlpha = 0.35f, borderAlpha = 0.4f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Slate950),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = gorsel,
                                    contentDescription = kart.on ?: "",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .padding(8.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = kart.on ?: "",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = SlateWhite,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { onSeslendir() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Blue60.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
                            ) {
                                Text("🔊", fontSize = 16.sp)
                            }
                        }

                        // Pronunciation / Meaning
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = kart.arka ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "Detayları ve Notları Göster 🔄",
                            color = Blue60,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .background(Blue60.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    // Back Face
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = "${kart.on ?: ""} — Önemli Notlar",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Purple60
                            )
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Slate800, thickness = 2.dp)
                            Spacer(Modifier.height(16.dp))

                            // Bullet points
                            val maddeler = getTeoriKartDetaylari(kart.id, kart.on ?: "")
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(maddeler) { madde ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("⚡", color = Purple60, modifier = Modifier.padding(end = 8.dp))
                                        Text(
                                            text = madde,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SlateWhite
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ön Yüzü Göster 🔄",
                                    color = Purple60,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .background(Purple60.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onTeoriGec,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                border = BorderStroke(1.5.dp, Slate800),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate400)
            ) {
                Text("Egzersizlere Geç ⏭️", fontWeight = FontWeight.Bold)
            }

            val sonrakiMetin = if (mevcutTeoriIndex + 1 == teoriKartlari.size) "Egzersizlere Başla 🎉" else "Sonraki Kart ›"
            Button(
                onClick = onTeoriSonraki,
                modifier = Modifier
                    .weight(1.2f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue60)
            ) {
                Text(sonrakiMetin, fontWeight = FontWeight.Bold, color = SlateWhite)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun EslestirmeSoru(
    egzersiz: Egzersiz,
    onCevapVer: () -> Unit
) {
    val cifler = egzersiz.cifler
    if (cifler.isEmpty()) {
        Text("Hata: Çiftler bulunamadı.", color = RedError)
        return
    }

    val sollar = remember(egzersiz.id) { cifler.map { it.sol }.shuffled() }
    val saglar = remember(egzersiz.id) { cifler.map { it.sag }.shuffled() }

    var seciliSol by remember(egzersiz.id) { mutableStateOf<String?>(null) }
    var seciliSag by remember(egzersiz.id) { mutableStateOf<String?>(null) }
    var eslesenSoller by remember(egzersiz.id) { mutableStateOf(emptySet<String>()) }
    var eslesenSaglar by remember(egzersiz.id) { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(eslesenSoller.size) {
        if (eslesenSoller.size == cifler.size && cifler.isNotEmpty()) {
            onCevapVer()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sol Sütun
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sollar.forEach { item ->
                val eslesmis = eslesenSoller.contains(item)
                val secilimi = seciliSol == item

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable(enabled = !eslesmis) {
                            seciliSol = if (secilimi) null else item

                            if (seciliSol != null && seciliSag != null) {
                                val matches = cifler.any { it.sol == seciliSol && it.sag == seciliSag }
                                if (matches) {
                                    eslesenSoller = eslesenSoller + seciliSol!!
                                    eslesenSaglar = eslesenSaglar + seciliSag!!
                                }
                                seciliSol = null
                                seciliSag = null
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            eslesmis -> GreenSuccess.copy(alpha = 0.25f)
                            secilimi -> Blue60.copy(alpha = 0.2f)
                            else -> Slate900
                        }
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        when {
                            eslesmis -> GreenSuccess
                            secilimi -> Blue60
                            else -> Slate800
                        }
                    )
                ) {
                    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = item,
                            color = if (eslesmis) GreenSuccess else SlateWhite,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Sağ Sütun
        Column(
            modifier = Modifier.weight(1.2f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            saglar.forEach { item ->
                val eslesmis = eslesenSaglar.contains(item)
                val secilimi = seciliSag == item

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable(enabled = !eslesmis) {
                            seciliSag = if (secilimi) null else item

                            if (seciliSol != null && seciliSag != null) {
                                val matches = cifler.any { it.sol == seciliSol && it.sag == seciliSag }
                                if (matches) {
                                    eslesenSoller = eslesenSoller + seciliSol!!
                                    eslesenSaglar = eslesenSaglar + seciliSag!!
                                }
                                seciliSol = null
                                seciliSag = null
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            eslesmis -> GreenSuccess.copy(alpha = 0.25f)
                            secilimi -> Blue60.copy(alpha = 0.2f)
                            else -> Slate900
                        }
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        when {
                            eslesmis -> GreenSuccess
                            secilimi -> Blue60
                            else -> Slate800
                        }
                    )
                ) {
                    Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = item,
                            color = if (eslesmis) GreenSuccess else SlateWhite,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SıralamaSoru(
    egzersiz: Egzersiz,
    onCevapVer: (String) -> Unit
) {
    val dogruSira = egzersiz.adimlar
    if (dogruSira.isEmpty()) {
        Text("Hata: Adımlar bulunamadı.", color = RedError)
        return
    }

    var mevcutSira by remember(egzersiz.id) { mutableStateOf(dogruSira.shuffled()) }
    var kontrolEdildi by remember(egzersiz.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        mevcutSira.forEachIndexed { index, adim ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, shape = RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "☰",
                        color = Slate400,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = adim,
                        color = SlateWhite,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                if (index > 0 && !kontrolEdildi) {
                                    val mutable = mevcutSira.toMutableList()
                                    val tmp = mutable[index]
                                    mutable[index] = mutable[index - 1]
                                    mutable[index - 1] = tmp
                                    mevcutSira = mutable
                                }
                            },
                            modifier = Modifier.size(28.dp),
                            enabled = index > 0 && !kontrolEdildi
                        ) {
                            Text("▲", color = if (index > 0 && !kontrolEdildi) Blue60 else Slate700, fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = {
                                if (index < mevcutSira.size - 1 && !kontrolEdildi) {
                                    val mutable = mevcutSira.toMutableList()
                                    val tmp = mutable[index]
                                    mutable[index] = mutable[index + 1]
                                    mutable[index + 1] = tmp
                                    mevcutSira = mutable
                                }
                            },
                            modifier = Modifier.size(28.dp),
                            enabled = index < mevcutSira.size - 1 && !kontrolEdildi
                        ) {
                            Text("▼", color = if (index < mevcutSira.size - 1 && !kontrolEdildi) Blue60 else Slate700, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                kontrolEdildi = true
                val isCorrect = mevcutSira == dogruSira
                if (isCorrect) {
                    onCevapVer("dogru")
                } else {
                    onCevapVer("yanlis")
                }
            },
            enabled = !kontrolEdildi,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue60),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Sıralamayı Kontrol Et ›", fontWeight = FontWeight.Bold)
        }
    }
}

private fun getTipEtiketi(tip: String): String {
    return when (tip) {
        "coktan_secmeli" -> "ÇOKTAN SEÇMELİ"
        "dogru_yanlis"   -> "DOĞRU / YANLIŞ"
        "bosluk_doldur"  -> "BOŞLUK DOLDUR"
        "sayisal", "hesapla" -> "SAYISAL FORMÜL HESABI"
        "kelime_hece"     -> "NATO KODLAMA"
        "cumle_tamamla"   -> "CÜMLE TAMAMLAMA"
        "formul_tamamla"  -> "FORMÜL TAMAMLAMA"
        "eslestir", "eslestime" -> "KART EŞLEŞTİRME"
        "sirala"          -> "PROSEDÜR SIRALAMA"
        "dinle_sec"       -> "DİNLE VE SEÇ"
        "cumle_dinle"     -> "DİNLE VE CÜMLEYİ SEÇ"
        "anlam_bul"       -> "Q KODU ANLAMI"
        "kodu_bul"        -> "Q KODU BUL"
        "diyalog_tamamla" -> "DİYALOG TAMAMLAMA"
        "diyalog"         -> "TELSİZ DİYALOGU"
        "durum_sec"       -> "DURUM SENARYOSU"
        "kodu_yaz"        -> "NATO KODU YAZIN"
        "harfi_yaz"       -> "HARFİ YAZIN"
        "hizli_tur"       -> "HIZLI TUR"
        else             -> tip.uppercase()
    }
}

private fun getFeedbackImage(aciklama: String): String? {
    val regex = Regex("<img[^>]+src=['\"]([^'\"]+)['\"]")
    val match = regex.find(aciklama)
    val path = match?.groupValues?.get(1) ?: return null
    val filename = path.substringAfterLast('/')
    return "file:///android_asset/img/$filename"
}

@Composable
fun HizliTurSoru(
    egzersiz: Egzersiz,
    onCevapVer: (String) -> Unit
) {
    val sorular = egzersiz.sorular
    if (sorular.isEmpty()) {
        Text("Hata: Hızlı tur soruları bulunamadı.", color = RedError)
        return
    }

    var mevcutIndex by remember { mutableStateOf(0) }
    var sureSol by remember { mutableStateOf(egzersiz.sure) }
    var skor by remember { mutableStateOf(0) }
    var bitti by remember { mutableStateOf(false) }

    val seceneklerKarilmis = remember(mevcutIndex) {
        val s = sorular.getOrNull(mevcutIndex)?.secenekler ?: emptyList()
        s.shuffled()
    }

    LaunchedEffect(key1 = bitti) {
        if (!bitti) {
            while (sureSol > 0) {
                kotlinx.coroutines.delay(1000)
                sureSol--
            }
            bitti = true
            onCevapVer(if (skor > 0) "dogru" else "yanlis")
        }
    }

    val numLabels = listOf("A", "B", "C", "D")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (sureSol <= 3) RedError.copy(alpha = 0.15f) else Slate900,
                border = BorderStroke(1.5.dp, if (sureSol <= 3) RedError else Slate800)
            ) {
                Text(
                    text = "⏱️ Süre: $sureSol sn",
                    color = if (sureSol <= 3) RedError else SlateWhite,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Blue60.copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, Blue60)
            ) {
                Text(
                    text = "🎯 Skor: $skor",
                    color = Blue60,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        val mevcutSoru = sorular.getOrNull(mevcutIndex)
        if (mevcutSoru != null && !bitti) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Slate900, shape = CircleShape)
                    .border(2.dp, Blue60, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mevcutSoru.gosterilen,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = SlateWhite
                )
            }

            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                seceneklerKarilmis.forEachIndexed { i, secenek ->
                    Button(
                        onClick = {
                            if (!bitti) {
                                val dogru = secenek == mevcutSoru.dogru
                                if (dogru) {
                                    skor++
                                }
                                if (mevcutIndex + 1 >= sorular.size) {
                                    bitti = true
                                    onCevapVer(if (skor > 0) "dogru" else "yanlis")
                                } else {
                                    mevcutIndex++
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        border = BorderStroke(1.dp, Slate800),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = numLabels.getOrNull(i) ?: "•",
                                color = Blue60,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = secenek,
                                color = SlateWhite,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (skor > 0) "⚡ Hızlı Tur Bitti! Skorunuz: $skor" else "⏱️ Süre Doldu!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (skor > 0) GreenSuccess else RedError
                )
            }
        }
    }
}

private fun getTeoriKartDetaylari(id: String, kod: String): List<String> {
    val lowerId = id.lowercase()
    val list = mutableListOf<String>()

    if (lowerId.contains("nato_")) {
        list.add("Uluslararası Havacılık ve Telsiz haberleşmesinde harflerin karışıklığını önlemek için kullanılır.")
        list.add("Çağrı işaretinizi karşı tarafa bildirirken bu fonetik kodlamayı kelime kelime heceleyerek yapmalısınız.")
        list.add("Sınavda çağrı işaretleri kodlaması sorulduğunda harflerin buradaki resmi karşılıklarını eksiksiz yazmanız istenir.")
    } else if (lowerId.contains("q_")) {
        list.add("Üç harfli uluslararası telsiz kısaltmalarıdır. İletişimi hızlandırır ve dil bariyerini ortadan kaldırır.")
        list.add("Koda soru işareti ekleyerek soru sorulur (Örn: $kod?). Soru işaretsiz ise cevap veya bilgi beyanıdır.")
        list.add("Q kodlarının tam Türkçe anlamları sınav sorularında doğrudan test edilmektedir.")
    } else if (lowerId.contains("el_")) {
        list.add("Temel elektronik prensipleri telsiz devresi tasarımı ve çalışma güvenliği için kritik öneme sahiptir.")
        if (lowerId.contains("ohm")) {
            list.add("Formül: Gerilim (V) = Akım (I) x Direnç (R). Üçgendeki yatay çizgi bölmeyi, dikey çizgi çarpmayı ifade eder.")
        } else if (lowerId.contains("guc")) {
            list.add("Telsiz çıkış gücü Watt ile ölçülür. P = V x I formülü ile güç kaynağından çekilen akım hesaplanabilir.")
        } else {
            list.add("Elektronik devre elemanlarının (direnç, bobin, kondansatör, diyot) sembollerini ve temel işlevlerini öğrenin.")
        }
        list.add("Sınavda basit V=IR ve P=VI formül hesaplamaları ile devre elemanlarının görevleri sorulur.")
    } else if (lowerId.contains("bn_")) {
        list.add("Frekans ve dalga boyu ters orantılıdır: Frekans yükseldikçe dalga boyu kısalır.")
        if (lowerId.contains("iyonosfer") || lowerId.contains("hf")) {
            list.add("Gök dalgası (skywave) yayılımı iyonosfer tabakasının yansıtma gücüne bağlıdır ve Güneş aktivitelerinden etkilenir.")
        } else {
            list.add("VHF/UHF bantları yerel görüşmelerde ve röle (repeater) sistemlerinde kullanılır. Görüş hattı yayılımı hakimdir.")
        }
        list.add("Lisans sınıflarına (A/B/C) ait bant limitleri ve dalga boyu formülü sınavda kesinlikle çıkar.")
    } else if (lowerId.contains("pr_")) {
        list.add("Telsiz işletmesinde konuşma disiplini ve standart kelimelerin kullanılması kritik öneme sahiptir.")
        if (lowerId.contains("mayday") || lowerId.contains("pan")) {
            list.add("Acil durum çağrıları önceliklidir ve frekanstaki diğer tüm telsiz görüşmeleri anında durdurulur.")
        } else {
            list.add("Çağrı işaretlerinin doğru ve anlaşılır telaffuzu telsiz ruhsatı kuralları gereğidir.")
        }
        list.add("Tehlike durumlarında yapılan tehlike (MAYDAY) ve acele (PAN-PAN) anonslarının farkları ve kullanım yerleri sorulur.")
    } else if (lowerId.contains("sv_")) {
        list.add("Türkiye'de amatör telsizcilik faaliyetleri Kıyı Emniyeti Genel Müdürlüğü ve BTK mevzuatlarına bağlıdır.")
        list.add("Lisans belgesi 10 yıl geçerlidir. Belge sahipleri telsiz kanunu kurallarına ve işletim kurallarına uymakla yükümlüdür.")
        list.add("Sınavda başarılı olmak için puan barajları (A sınıfı 75, B sınıfı 60) ve mevzuat cezaları test edilir.")
    } else {
        list.add("Temel radyo amatörlüğü kavramı ve kuramsal bilgi.")
        list.add("Dersteki sorulara hazırlık olmak üzere açıklamaları dikkatlice okuyunuz.")
        list.add("Bu konudaki terimlerin anlamları sınavda soru olarak karşınıza çıkacaktır.")
    }
    return list
}

private fun getTeoriKartGorselYolu(id: String, kod: String, aciklama: String): String {
    val lowerId = id.lowercase()
    val lowerKod = kod.lowercase()
    val lowerDesc = aciklama.lowercase()

    val name = when {
        lowerId.contains("swr") -> "swr_meter.png"
        lowerId.contains("anten") -> "anten_tipleri.svg"
        lowerId.contains("rst") -> "rf_spectrum.png"
        lowerId.contains("kanun") -> "telsiz.png"
        lowerId.contains("btk") -> "ham_shack.png"
        lowerId.contains("sinav_kural") -> "telsiz.png"
        lowerId.contains("cq_dx") -> "ham_shack.png"
        lowerId.contains("mayday") -> "mayday_boat.png"
        lowerId.contains("q_kodlari") -> "qsl_card.png"
        lowerId.contains("hesaplama") -> "ohm_ucgeni.svg"
        lowerId.contains("dalga_boyu") -> "dalga_boyu.svg"
        lowerId.contains("yayilim_modlari") -> "skywave.svg"
        lowerId.contains("modlar") -> "telegraph_key.png"
        lowerId.contains("modern_dijital") -> "ham_shack.png"
        lowerId.contains("ileri_yayilim") -> "skywave.svg"
        lowerId.contains("emniyet") -> "devre.png"
        lowerId.contains("gps_lokator") -> "repeater.svg"
        lowerId.contains("tekrar_ozet") -> "telsiz.png"

        lowerId.contains("nato_") -> {
            if (listOf("alpha", "bravo", "charlie").any { lowerId.contains(it) }) "telegraph_key.png"
            else if (listOf("delta", "echo", "foxtrot").any { lowerId.contains(it) }) "el_telsizi.svg"
            else "telsiz.png"
        }

        lowerId.contains("q_") || lowerKod.startsWith("q") -> {
            if (lowerId.contains("qsl")) "qsl_card.png"
            else if (lowerId.contains("qrs")) "telegraph_key.png"
            else if (lowerId.contains("qrm") || lowerId.contains("qrn")) "rf_spectrum.png"
            else "ham_shack.png"
        }

        lowerId.contains("el_") -> {
            if (listOf("volt", "amper", "ohm", "watt").any { lowerId.contains(it) }) "ohm_ucgeni.svg"
            else if (listOf("direnc", "kondansator", "bobin").any { lowerId.contains(it) }) "devre_elemanlari.svg"
            else "devre.png"
        }

        lowerDesc.contains("dalga boyu") -> "dalga_boyu.svg"
        lowerDesc.contains("iyonosfer") || lowerDesc.contains("skywave") -> "skywave.svg"
        lowerDesc.contains("röle") || lowerDesc.contains("repeater") -> "repeater.svg"
        lowerDesc.contains("ohm") || lowerDesc.contains("gerilim") -> "ohm_ucgeni.svg"
        lowerDesc.contains("direnç") || lowerDesc.contains("kondansatör") -> "devre_elemanlari.svg"
        lowerDesc.contains("mayday") || lowerDesc.contains("pan-pan") || lowerDesc.contains("acil durum") -> "mayday_boat.png"
        lowerDesc.contains("çağrı işareti") || lowerDesc.contains("telsiz görüşmesi") -> "ham_shack.png"

        else -> "telsiz.png"
    }
    return "file:///android_asset/img/$name"
}

@Composable
private fun OzetEkrani(
    viewModel: ExerciseViewModel,
    onTamamlandi: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val oturum = uiState.oturum
    val puan = oturum.puan
    val yildiz = when {
        puan >= 90 -> "⭐⭐⭐"
        puan >= 70 -> "⭐⭐"
        puan >= 50 -> "⭐"
        else -> ""
    }
    val gecti = puan >= uiState.gecmePuani || uiState.mod == "ders" || uiState.mod == "pratik"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp))

        Text(text = if (gecti) "🎉" else "😔", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = when (uiState.mod) {
                "ders"   -> "Ders Tamamlandı!"
                "pratik" -> "Harika İş!"
                else     -> if (gecti) "Sınavı Geçtiniz!" else "Sınav Tamamlandı"
            },
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = SlateWhite,
            textAlign = TextAlign.Center
        )

        if (yildiz.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(text = yildiz, fontSize = 32.sp)
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (uiState.mod != "ders" && uiState.mod != "pratik") {
                StatItem(deger = "%$puan", baslik = "Puan", renk = if (gecti) GreenSuccess else RedError)
            }
            StatItem(deger = "${oturum.dogruSayisi}", baslik = "Doğru", renk = GreenSuccess)
            StatItem(deger = "${oturum.yanlisSayisi}", baslik = "Yanlış", renk = RedError)
        }

        if (uiState.yeniRozetler.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Text("🏆 Yeni Rozet!", style = MaterialTheme.typography.titleMedium, color = GoldXP)
            Spacer(Modifier.height(12.dp))
            uiState.yeniRozetler.forEach { rozetId ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    BadgeIcon(rozetId = rozetId, kazanildi = true, size = 44.dp)
                    Text(
                        text = GamificationEngine.getRozetIsim(rozetId),
                        color = SlateWhite,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onTamamlandi,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue60),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Devam Et", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SlateWhite)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatItem(deger: String, baslik: String, renk: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = deger,
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = renk
        )
        Text(text = baslik, style = MaterialTheme.typography.labelLarge, color = Slate400)
    }
}
