package com.telsizokulu.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.telsizokulu.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "telsiz_okulu_ilerleme")

/**
 * Kullanıcı ilerlemesini Jetpack DataStore üzerinde yönetir.
 * Web'deki TelsizProgress (localStorage) sınıfının tam karşılığıdır.
 */
class ProgressRepository(private val context: Context) {

    private val gson = Gson()
    private val PROGRESS_KEY = stringPreferencesKey("ilerleme_json")
    private val ONBOARDING_KEY = booleanPreferencesKey("onboarding_goruldu")

    // ── Onboarding ─────────────────────────────────────────────

    suspend fun getOnboardingGoruldu(): Boolean =
        context.dataStore.data.map { it[ONBOARDING_KEY] ?: false }.first()

    suspend fun setOnboardingGoruldu() {
        context.dataStore.edit { it[ONBOARDING_KEY] = true }
    }

    // ── Okuma ──────────────────────────────────────────────────

    val ilerlemFlow: Flow<KullaniciIlerleme> = context.dataStore.data.map { prefs ->
        val json = prefs[PROGRESS_KEY]
        if (json != null) {
            try {
                gson.fromJson(json, KullaniciIlerleme::class.java)
            } catch (e: Exception) {
                varsayilanOlustur()
            }
        } else {
            varsayilanOlustur()
        }
    }

    suspend fun getIlerleme(): KullaniciIlerleme {
        return ilerlemFlow.first()
    }

    // ── Yazma / Kaydetme ──────────────────────────────────────

    suspend fun save(ilerleme: KullaniciIlerleme) {
        context.dataStore.edit { prefs ->
            prefs[PROGRESS_KEY] = gson.toJson(ilerleme)
        }
    }

    // ── Streak Güncelle ───────────────────────────────────────

    suspend fun updateStreak(): Int {
        val ilerleme = getIlerleme()
        val bugun = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val sonGiris = ilerleme.kullanici.sonGiris

        if (sonGiris == bugun) return ilerleme.kullanici.streak

        val dun = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val yeniStreak = when (sonGiris) {
            dun -> ilerleme.kullanici.streak + 1
            else -> 1
        }

        val gunlukDers = if (ilerleme.kullanici.gunlukDersTarih != bugun) 0 else ilerleme.kullanici.gunlukDers

        val updated = ilerleme.copy(
            kullanici = ilerleme.kullanici.copy(
                streak = yeniStreak,
                enYuksekStreak = maxOf(yeniStreak, ilerleme.kullanici.enYuksekStreak),
                sonGiris = bugun,
                gunlukDers = gunlukDers,
                gunlukDersTarih = bugun
            )
        )
        save(updated)
        return yeniStreak
    }

    // ── XP Ekle ────────────────────────────────────────────────

    suspend fun addXP(miktar: Int): AddXPResult {
        val ilerleme = getIlerleme()
        val yeniXP = ilerleme.kullanici.xp + miktar
        val eskiSeviye = ilerleme.kullanici.seviye
        val yeniSeviye = hesaplaSeviye(yeniXP)

        val updated = ilerleme.copy(
            kullanici = ilerleme.kullanici.copy(
                xp = yeniXP,
                seviye = yeniSeviye
            )
        )
        save(updated)
        return AddXPResult(
            toplamXP = yeniXP,
            seviyeAtladi = yeniSeviye > eskiSeviye,
            yeniSeviye = yeniSeviye
        )
    }

    // ── Ders Tamamla ──────────────────────────────────────────

    suspend fun dersTamamla(bolumId: String, altBolumId: String, dersNo: Int) {
        val ilerleme = getIlerleme().toMutable()
        val bugun = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val altBolum = ilerleme.bolumler[bolumId]?.altBolumler?.get(altBolumId) ?: return
        val tamamlananDersler = altBolum.tamamlananDersler.toMutableList()
        if (dersNo !in tamamlananDersler) tamamlananDersler.add(dersNo)

        val gunlukDers = if (ilerleme.kullanici.gunlukDersTarih != bugun) 1
        else ilerleme.kullanici.gunlukDers + 1

        val gunlukDersTarih = bugun

        ilerleme.bolumler = ilerleme.bolumler.toMutableMap().apply {
            val bolum = get(bolumId) ?: return
            put(bolumId, bolum.copy(
                altBolumler = bolum.altBolumler.toMutableMap().apply {
                    put(altBolumId, altBolum.copy(tamamlananDersler = tamamlananDersler))
                }
            ))
        }
        ilerleme.kullanici = ilerleme.kullanici.copy(
            gunlukDers = gunlukDers,
            gunlukDersTarih = gunlukDersTarih,
            tamamlananDersler = ilerleme.kullanici.tamamlananDersler + 1
        )
        save(ilerleme.toImmutable())
    }

    // ── Alt Bölüm Tamamla ─────────────────────────────────────

    suspend fun tamamlaAltBolum(bolumId: String, altBolumId: String, puan: Int) {
        val ilerleme = getIlerleme().toMutable()
        val bolum = ilerleme.bolumler[bolumId] ?: return
        val altBolum = bolum.altBolumler[altBolumId] ?: return

        val yeniAltBolumler = bolum.altBolumler.toMutableMap()
        yeniAltBolumler[altBolumId] = altBolum.copy(durum = "tamamlandi", puan = puan)

        // Sonraki alt bölümü aç
        val altBolumIdler = bolum.altBolumler.keys.toList()
        val mevcutIndex = altBolumIdler.indexOf(altBolumId)
        if (mevcutIndex >= 0 && mevcutIndex < altBolumIdler.size - 1) {
            val sonraki = altBolumIdler[mevcutIndex + 1]
            if (yeniAltBolumler[sonraki]?.durum == "kilitli") {
                yeniAltBolumler[sonraki] = yeniAltBolumler[sonraki]!!.copy(durum = "devam_ediyor")
            }
        }

        // Tümü tamamlandı mı? → Bölüm sınavını aç
        val tumTamamlandi = yeniAltBolumler.values.all { it.durum == "tamamlandi" }
        val yeniGecmeSinavi = if (tumTamamlandi) bolum.gecmeSinavi.copy(durum = "acik") else bolum.gecmeSinavi

        ilerleme.bolumler = ilerleme.bolumler.toMutableMap().apply {
            put(bolumId, bolum.copy(altBolumler = yeniAltBolumler, gecmeSinavi = yeniGecmeSinavi))
        }
        save(ilerleme.toImmutable())
    }

    // ── Bölüm Tamamla ─────────────────────────────────────────

    suspend fun tamamlaBolum(bolumId: String, puan: Int) {
        val ilerleme = getIlerleme().toMutable()
        val bolum = ilerleme.bolumler[bolumId] ?: return

        ilerleme.bolumler = ilerleme.bolumler.toMutableMap().apply {
            put(bolumId, bolum.copy(
                durum = "tamamlandi",
                gecmeSinavi = bolum.gecmeSinavi.copy(durum = "tamamlandi", puan = puan)
            ))
        }

        // Sonraki bölümü aç
        val bolumIdler = ilerleme.bolumler.keys.toList()
        val mevcutIndex = bolumIdler.indexOf(bolumId)
        if (mevcutIndex >= 0 && mevcutIndex < bolumIdler.size - 1) {
            val sonrakiBolumId = bolumIdler[mevcutIndex + 1]
            val sonrakiBolum = ilerleme.bolumler[sonrakiBolumId]
            if (sonrakiBolum?.durum == "kilitli") {
                val yeniAltBolumler = sonrakiBolum.altBolumler.toMutableMap()
                val ilkAltBolumId = sonrakiBolum.altBolumler.keys.firstOrNull()
                if (ilkAltBolumId != null) {
                    yeniAltBolumler[ilkAltBolumId] = yeniAltBolumler[ilkAltBolumId]!!.copy(durum = "devam_ediyor")
                }
                ilerleme.bolumler = ilerleme.bolumler.toMutableMap().apply {
                    put(sonrakiBolumId, sonrakiBolum.copy(durum = "devam_ediyor", altBolumler = yeniAltBolumler))
                }
            }
        }
        save(ilerleme.toImmutable())
    }

    // ── Rozet Kazan ────────────────────────────────────────────

    suspend fun rozetKazan(rozetId: String): Boolean {
        val ilerleme = getIlerleme()
        if (rozetId in ilerleme.kullanici.rozetler) return false
        val yeniRozetler = ilerleme.kullanici.rozetler + rozetId
        save(ilerleme.copy(kullanici = ilerleme.kullanici.copy(rozetler = yeniRozetler)))
        return true
    }

    // ── Kart Güncelle ─────────────────────────────────────────

    suspend fun kartGuncelle(kartId: String, dogruMu: Boolean) {
        val ilerleme = getIlerleme()
        val kart = ilerleme.kartlar[kartId] ?: KartDurum()
        val bugun = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        val yeniKart = if (dogruMu) {
            val artArdaDogru = (kart.artArdaDogru) + 1
            val durum = when {
                kart.durum == "ogrenmedim" && kart.dogru >= 0 -> "ogreniyor"
                kart.durum == "ogreniyor" && artArdaDogru >= 3 -> "ogrendim"
                else -> kart.durum
            }
            kart.copy(dogru = kart.dogru + 1, artArdaDogru = artArdaDogru, durum = durum, sonGorulme = bugun)
        } else {
            val durum = if (kart.durum == "ogrendim") "ogreniyor" else kart.durum
            kart.copy(yanlis = kart.yanlis + 1, artArdaDogru = 0, durum = durum, sonGorulme = bugun)
        }

        val yeniKartlar = ilerleme.kartlar.toMutableMap().apply { put(kartId, yeniKart) }
        save(ilerleme.copy(kartlar = yeniKartlar))
    }

    // ── Art Arda Hatasız ─────────────────────────────────────

    suspend fun artArdaHatasizGuncelle(hatasizMi: Boolean): Int {
        val ilerleme = getIlerleme()
        val yeni = if (hatasizMi) ilerleme.kullanici.artArdaHatasiz + 1 else 0
        save(ilerleme.copy(kullanici = ilerleme.kullanici.copy(artArdaHatasiz = yeni)))
        return yeni
    }

    // ── Sıfırlama ─────────────────────────────────────────────

    suspend fun sifirla() {
        save(varsayilanOlustur())
    }

    suspend fun sifirlaAltBolum(bolumId: String, altBolumId: String) {
        val ilerleme = getIlerleme().toMutable()
        val bolum = ilerleme.bolumler[bolumId] ?: return
        val altBolum = bolum.altBolumler[altBolumId] ?: return

        val yeniAltBolumler = bolum.altBolumler.toMutableMap()
        yeniAltBolumler[altBolumId] = altBolum.copy(
            durum = "devam_ediyor",
            puan = null,
            tamamlananDersler = emptyList()
        )

        ilerleme.bolumler = ilerleme.bolumler.toMutableMap().apply {
            put(bolumId, bolum.copy(altBolumler = yeniAltBolumler))
        }
        save(ilerleme.toImmutable())
    }

    suspend fun sifirlaBolum(bolumId: String) {
        val ilerleme = getIlerleme().toMutable()
        val bolum = ilerleme.bolumler[bolumId] ?: return

        val yeniAltBolumler = bolum.altBolumler.toMutableMap()
        val altBolumKeys = yeniAltBolumler.keys.toList()
        altBolumKeys.forEachIndexed { index, abId ->
            val ab = yeniAltBolumler[abId]
            if (ab != null) {
                yeniAltBolumler[abId] = ab.copy(
                    durum = if (index == 0) "devam_ediyor" else "kilitli",
                    puan = null,
                    tamamlananDersler = emptyList()
                )
            }
        }

        ilerleme.bolumler = ilerleme.bolumler.toMutableMap().apply {
            put(bolumId, bolum.copy(
                durum = "devam_ediyor",
                gecmeSinavi = bolum.gecmeSinavi.copy(durum = "kilitli", puan = null),
                altBolumler = yeniAltBolumler
            ))
        }
        save(ilerleme.toImmutable())
    }

    // ── Dışa Aktarma ─────────────────────────────────────────

    suspend fun exportJson(): String {
        return gson.toJson(getIlerleme())
    }

    suspend fun importJson(json: String) {
        val ilerleme = gson.fromJson(json, KullaniciIlerleme::class.java)
        save(ilerleme)
    }

    // ── Yardımcı ─────────────────────────────────────────────

    private fun varsayilanOlustur(): KullaniciIlerleme {
        val bugun = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return KullaniciIlerleme(
            kullanici = KullaniciData(sonGiris = bugun, gunlukDersTarih = bugun)
        )
    }

    fun hesaplaSeviye(xp: Int): Int {
        return when {
            xp >= 5000 -> 7
            xp >= 3500 -> 6
            xp >= 2000 -> 5
            xp >= 1000 -> 4
            xp >= 500  -> 3
            xp >= 200  -> 2
            else       -> 1
        }
    }

    fun getSeviyeIsim(seviye: Int): String = when (seviye) {
        1 -> "Acemi Telsizci"
        2 -> "Stajyer"
        3 -> "Amatör"
        4 -> "Deneyimli"
        5 -> "Uzman"
        6 -> "Usta Telsizci"
        7 -> "Telsiz Şampiyonu"
        else -> "Telsizci"
    }

    fun getSonrakiSeviyeXP(seviye: Int): Int = when (seviye) {
        1 -> 200; 2 -> 500; 3 -> 1000; 4 -> 2000; 5 -> 3500; 6 -> 5000; else -> 5000
    }
}

data class AddXPResult(val toplamXP: Int, val seviyeAtladi: Boolean, val yeniSeviye: Int)

// ── Mutable helper (deep copy için) ──────────────────────────────────────

private data class MutableIlerleme(
    var kullanici: KullaniciData,
    var bolumler: Map<String, BolumIlerleme>,
    var kartlar: Map<String, KartDurum>
)

private fun KullaniciIlerleme.toMutable() = MutableIlerleme(kullanici, bolumler, kartlar)
private fun MutableIlerleme.toImmutable() = KullaniciIlerleme(kullanici, bolumler, kartlar)
