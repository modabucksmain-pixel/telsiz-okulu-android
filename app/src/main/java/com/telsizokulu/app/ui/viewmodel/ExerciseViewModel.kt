package com.telsizokulu.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.telsizokulu.app.data.model.Egzersiz
import com.telsizokulu.app.data.model.IcerikDosyasi
import com.telsizokulu.app.data.model.Kart
import com.telsizokulu.app.data.repository.CurriculumRepository
import com.telsizokulu.app.data.repository.ProgressRepository
import com.telsizokulu.app.engine.AudioEngine
import com.telsizokulu.app.engine.ExerciseEngine
import com.telsizokulu.app.engine.EgzersizOturumu
import com.telsizokulu.app.engine.GamificationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExerciseUiState(
    val oturum: EgzersizOturumu = EgzersizOturumu(emptyList()),
    val icerik: IcerikDosyasi = IcerikDosyasi(),
    val yukleniyor: Boolean = true,
    val xpKazanildi: Int = 0,
    val showXpAnimation: Boolean = false,
    val yeniRozetler: List<String> = emptyList(),
    val gecmePuani: Int = 60,
    val soruSayisi: Int = 8,
    val mod: String = "ders",          // "ders" | "sinav" | "pratik" | "alt_bolum" | "bolum" | "genel"
    val bolumId: String = "",
    val altBolumId: String = "",
    val dersNo: Int = 1,
    val teoriKartlari: List<Kart> = emptyList(),
    val mevcutTeoriIndex: Int = 0,
    val teoriModu: Boolean = false,
    val kartCevrildi: Boolean = false,
    val hata: String? = null
)

class ExerciseViewModel(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
    private val audioEngine: AudioEngine,
    private val bolumId: String,
    private val altBolumId: String,
    private val dersNo: Int,
    private val mod: String    // "ders" | "sinav_alt_bolum" | "sinav_bolum" | "sinav_genel" | "pratik"
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseUiState(mod = mod, bolumId = bolumId, altBolumId = altBolumId, dersNo = dersNo))
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private val engine = ExerciseEngine()

    init {
        viewModelScope.launch {
            try { yukle() } catch (e: Throwable) {
                e.printStackTrace()
                val trace = e.stackTrace.take(4).joinToString("\n") { "  at $it" }
                _uiState.value = _uiState.value.copy(
                    yukleniyor = false,
                    hata = "${e::class.java.simpleName}: ${e.message}\n$trace"
                )
            }
        }
    }

    private suspend fun yukle() {
        _uiState.value = _uiState.value.copy(yukleniyor = true)

        val curriculum = curriculumRepo.loadCurriculum()
        val ilerleme = progressRepo.getIlerleme()

        // Bölümün içeriğini belirle
        val veriDosyasi = curriculum.bolumler.find { it.id == bolumId }?.veriDosyasi ?: ""
        var icerik = IcerikDosyasi()
        var loadedTeoriKartlari = emptyList<Kart>()
        var isTeoriModu = false

        val (egzersizler, gecmePuani, soruSayisi) = when (mod) {
            "ders" -> {
                icerik = if (veriDosyasi.isNotEmpty()) curriculumRepo.loadIcerik(veriDosyasi) else IcerikDosyasi()
                val altBolum = curriculum.bolumler.find { it.id == bolumId }
                    ?.altBolumler?.find { it.id == altBolumId }
                val kartIds = altBolum?.dersler?.find { it.no == dersNo }?.kartlar ?: emptyList()
                
                if (icerik.kartlar.isNotEmpty() && kartIds.isNotEmpty()) {
                    loadedTeoriKartlari = icerik.kartlar.filter { k -> kartIds.contains(k.id) }
                }
                
                if (loadedTeoriKartlari.isEmpty() && icerik.kartlar.isNotEmpty()) {
                    val subSectionCards = icerik.kartlar.filter { 
                        it.id.startsWith("nato_") || it.id.startsWith("q_") || 
                        it.id.startsWith("el_") || it.id.startsWith("bn_") || 
                        it.id.startsWith("pr_") || it.id.startsWith("sv_")
                    }.filter { 
                        it.id.contains(altBolumId) || 
                        (altBolumId.length >= 3 && it.id.contains(altBolumId.substring(1)))
                    }
                    if (subSectionCards.isNotEmpty()) {
                        val size = Math.ceil(subSectionCards.size.toDouble() / 3.0).toInt()
                        loadedTeoriKartlari = when (dersNo) {
                            1 -> subSectionCards.take(size)
                            2 -> subSectionCards.drop(size).take(size)
                            else -> subSectionCards.drop(size * 2)
                        }
                    }
                }
                
                isTeoriModu = loadedTeoriKartlari.isNotEmpty()
                Triple(engine.dersEgzersizleriOlustur(icerik, kartIds, altBolumId, 8, ilerleme.kartlar), 0, 8)
            }
            "alt_bolum" -> {
                icerik = if (veriDosyasi.isNotEmpty()) curriculumRepo.loadIcerik(veriDosyasi) else IcerikDosyasi()
                val gp = curriculum.bolumler.find { it.id == bolumId }
                    ?.altBolumler?.find { it.id == altBolumId }?.sinav?.gecmePuani ?: 60
                val ss = curriculum.bolumler.find { it.id == bolumId }
                    ?.altBolumler?.find { it.id == altBolumId }?.sinav?.soruSayisi ?: 15
                Triple(engine.sinavEgzersizleriOlustur(icerik, altBolumId, ss, ilerleme.kartlar), gp, ss)
            }
            "bolum" -> {
                icerik = if (veriDosyasi.isNotEmpty()) curriculumRepo.loadIcerik(veriDosyasi) else IcerikDosyasi()
                val gp = curriculum.bolumler.find { it.id == bolumId }?.gecmeSinavi?.gecmePuani ?: 70
                val ss = curriculum.bolumler.find { it.id == bolumId }?.gecmeSinavi?.soruSayisi ?: 20
                Triple(engine.sinavEgzersizleriOlustur(icerik, "", ss), gp, ss)
            }
            "genel" -> {
                icerik = curriculumRepo.loadTumIcerik()
                Triple(engine.sinavEgzersizleriOlustur(icerik, "", 50), 75, 50)
            }
            "pratik" -> {
                icerik = curriculumRepo.loadTumIcerik()
                val sayisi = minOf(10, ilerleme.kartlar.count { (_, k) -> k.yanlis > 0 && k.durum != "ogrendim" } * 2)
                Triple(engine.pratikEgzersizleriOlustur(icerik, ilerleme.kartlar, sayisi), 0, sayisi)
            }
            else -> Triple(emptyList(), 0, 8)
        }

        _uiState.value = _uiState.value.copy(
            oturum = EgzersizOturumu(egzersizler),
            icerik = icerik,
            gecmePuani = gecmePuani,
            soruSayisi = soruSayisi,
            teoriKartlari = loadedTeoriKartlari,
            teoriModu = isTeoriModu,
            yukleniyor = false
        )

        if (isTeoriModu) {
            seslendirMevcutTeori()
        }
    }

    fun teoriCevir() {
        _uiState.value = _uiState.value.copy(kartCevrildi = !_uiState.value.kartCevrildi)
    }

    fun teoriSonraki() {
        val nextIndex = _uiState.value.mevcutTeoriIndex + 1
        if (nextIndex >= _uiState.value.teoriKartlari.size) {
            _uiState.value = _uiState.value.copy(
                teoriModu = false,
                mevcutTeoriIndex = 0,
                kartCevrildi = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                mevcutTeoriIndex = nextIndex,
                kartCevrildi = false
            )
            seslendirMevcutTeori()
        }
    }

    fun teoriGec() {
        _uiState.value = _uiState.value.copy(
            teoriModu = false,
            mevcutTeoriIndex = 0,
            kartCevrildi = false
        )
    }

    /** dinle_sec / cumle_dinle sorusunda hedef sesi (NATO kelimesi) telsiz efektiyle çalar. */
    fun seslendirDinle(egzersiz: Egzersiz) {
        val kartId = egzersiz.dogruKart.ifEmpty { egzersiz.kartId }
        if (kartId.startsWith("nato_")) {
            val kelime = kartId.removePrefix("nato_").replaceFirstChar { it.uppercase() }
            audioEngine.oynatNATO(kelime)
        } else {
            val text = egzersiz.cevap.ifEmpty { egzersiz.soru }
            audioEngine.seslendir(text, "en-US")
        }
    }

    fun seslendirMevcutTeori() {
        val kart = _uiState.value.teoriKartlari.getOrNull(_uiState.value.mevcutTeoriIndex) ?: return
        val nato = kart.id.startsWith("nato_")
        val lang = if (nato) "en-US" else "tr-TR"
        // NATO kartında okunacak şey kod kelimesi (Alpha), harf değil (A).
        val text = if (nato) (kart.arka ?: "").ifEmpty { kart.on ?: "" }
                   else (kart.on ?: "").ifEmpty { kart.arka ?: "" }
        audioEngine.seslendir(text.ifEmpty { kart.id }, lang)
    }

    /**
     * Kullanıcı bir cevap gönderdiğinde çağrılır.
     */
    fun cevapVer(cevap: String) {
        val oturum = _uiState.value.oturum
        val egzersiz = oturum.mevcutEgzersiz ?: return
        val dogru = cevapDogruMu(egzersiz, cevap)

        viewModelScope.launch {
            // Kart durumunu güncelle
            if (egzersiz.kartId.isNotEmpty()) {
                progressRepo.kartGuncelle(egzersiz.kartId, dogru)
            }

            // XP ekle (ders modunda)
            if (mod == "ders" || mod == "pratik") {
                val xp = if (dogru) {
                    when (egzersiz.zorluk) {
                        "kolay" -> GamificationEngine.XP.DOGRU_KOLAY
                        "zor"   -> GamificationEngine.XP.DOGRU_ZOR
                        else    -> GamificationEngine.XP.DOGRU_ORTA
                    }
                } else 0
                if (xp > 0) {
                    progressRepo.addXP(xp)
                    _uiState.value = _uiState.value.copy(xpKazanildi = xp, showXpAnimation = true)
                }
            }

            // Ses efekti
            audioEngine.oynatEfekt(if (dogru) "dogru" else "yanlis")

            // NATO sesi (ders modundaysa ve kart ses var ise)
            if (egzersiz.kartId.startsWith("nato_") && mod == "ders") {
                val kelime = egzersiz.kartId.removePrefix("nato_")
                    .replaceFirstChar { it.uppercase() }
                audioEngine.oynatNATO(kelime)
            }

            val yeniOturum = oturum.copy(
                dogruSayisi = if (dogru) oturum.dogruSayisi + 1 else oturum.dogruSayisi,
                yanlisSayisi = if (!dogru) oturum.yanlisSayisi + 1 else oturum.yanlisSayisi,
                geribildirımGoruntu = true,
                sonCevapDogru = dogru
            )
            _uiState.value = _uiState.value.copy(oturum = yeniOturum)
        }
    }

    /**
     * "Devam Et" butonuna basılınca bir sonraki soruya geç.
     */
    fun devamEt() {
        val oturum = _uiState.value.oturum
        val sonraki = oturum.mevcutIndex + 1

        if (sonraki >= oturum.egzersizler.size) {
            oturumTamamla()
        } else {
            _uiState.value = _uiState.value.copy(
                oturum = oturum.copy(
                    mevcutIndex = sonraki,
                    geribildirımGoruntu = false,
                    sonCevapDogru = null
                ),
                showXpAnimation = false
            )
        }
    }

    private fun oturumTamamla() {
        val oturum = _uiState.value.oturum
        val puan = oturum.puan

        viewModelScope.launch {
            when (mod) {
                "ders" -> {
                    progressRepo.dersTamamla(bolumId, altBolumId, dersNo)
                    val hatasiz = oturum.yanlisSayisi == 0
                    progressRepo.artArdaHatasizGuncelle(hatasiz)
                    if (hatasiz) progressRepo.addXP(GamificationEngine.XP.HATASIZ_DERS)
                }
                "alt_bolum" -> {
                    if (puan >= _uiState.value.gecmePuani) {
                        progressRepo.tamamlaAltBolum(bolumId, altBolumId, puan)
                        progressRepo.addXP(GamificationEngine.XP.ALT_BOLUM_SINAV)
                    } else {
                        progressRepo.sifirlaAltBolum(bolumId, altBolumId)
                    }
                }
                "bolum" -> {
                    if (puan >= _uiState.value.gecmePuani) {
                        progressRepo.tamamlaBolum(bolumId, puan)
                        progressRepo.addXP(GamificationEngine.XP.BOLUM_SINAV)
                    } else {
                        progressRepo.sifirlaBolum(bolumId)
                    }
                }
                "genel" -> {
                    progressRepo.addXP(GamificationEngine.XP.BOLUM_SINAV)
                }
            }

            // Rozet kontrolü
            val guncelIlerleme = progressRepo.getIlerleme()
            val yeniRozetler = GamificationEngine.kontrolEt(guncelIlerleme)
            for (rozetId in yeniRozetler) {
                progressRepo.rozetKazan(rozetId)
            }

            audioEngine.oynatEfekt("bolum")

            _uiState.value = _uiState.value.copy(
                oturum = oturum.copy(tamamlandi = true),
                yeniRozetler = yeniRozetler
            )
        }
    }

    fun xpAnimasyonGizle() {
        _uiState.value = _uiState.value.copy(showXpAnimation = false)
    }

    private fun normalizeString(str: String): String {
        var result = str.trim().lowercase()
        result = result.replace('ğ', 'g')
        result = result.replace('ü', 'u')
        result = result.replace('ş', 's')
        result = result.replace('ı', 'i')
        result = result.replace('ö', 'o')
        result = result.replace('ç', 'c')
        result = result.replace("i̇", "i")
        result = result.replace("\u0307", "") // remove combining dot above
        result = result.replace(Regex("[-\\s]"), "")
        return result
    }

    private fun cevapDogruMu(egzersiz: Egzersiz, kullaniciCevap: String): Boolean {
        val dogruCevap = egzersiz.cevap
        return when (egzersiz.tip) {
            "dogru_yanlis" -> {
                val expected = if (dogruCevap.trim().equals("Doğru", ignoreCase = true)) "dogru" else "yanlis"
                kullaniciCevap.trim().equals(expected, ignoreCase = true)
            }
            "eslestir", "eslestime", "sirala" -> {
                kullaniciCevap.trim().equals("dogru", ignoreCase = true)
            }
            "sayisal", "hesapla" -> {
                val vNum = kullaniciCevap.trim().replace(",", ".").toDoubleOrNull() ?: return false
                val bNum = dogruCevap.trim().replace(",", ".").toDoubleOrNull() ?: return false
                val fark = Math.abs(vNum - bNum)
                val yuzdeFark = if (bNum != 0.0) fark / Math.abs(bNum) else fark
                yuzdeFark <= 0.05 || fark <= 0.2
            }
            else -> {
                normalizeString(kullaniciCevap) == normalizeString(dogruCevap)
            }
        }
    }
}

class ExerciseViewModelFactory(
    private val curriculumRepo: CurriculumRepository,
    private val progressRepo: ProgressRepository,
    private val audioEngine: AudioEngine,
    private val bolumId: String,
    private val altBolumId: String,
    private val dersNo: Int,
    private val mod: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExerciseViewModel(
            curriculumRepo, progressRepo, audioEngine,
            bolumId, altBolumId, dersNo, mod
        ) as T
    }
}
