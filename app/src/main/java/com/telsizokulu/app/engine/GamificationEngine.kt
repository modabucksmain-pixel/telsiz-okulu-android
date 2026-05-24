package com.telsizokulu.app.engine

import com.telsizokulu.app.data.model.KullaniciIlerleme
import com.telsizokulu.app.data.model.RozetTanim
import com.telsizokulu.app.data.repository.ProgressRepository

/**
 * GamificationEngine — Web'deki Gamification JS sınıfının Kotlin karşılığı.
 * XP miktarları, rozet koşulları ve kazanım mantığını yönetir.
 */
object GamificationEngine {

    // ── XP Miktarları ────────────────────────────────────────
    object XP {
        const val DOGRU_KOLAY = 10
        const val DOGRU_ORTA = 15
        const val DOGRU_ZOR = 20
        const val ALT_BOLUM_SINAV = 50
        const val BOLUM_SINAV = 150
        const val GUNLUK_HEDEF = 30
        const val HATASIZ_DERS = 25
    }

    // ── Seviye İsimleri ───────────────────────────────────────
    val SEVIYE_ISIMLERI = mapOf(
        1 to "Acemi Telsizci",
        2 to "Stajyer",
        3 to "Amatör",
        4 to "Deneyimli",
        5 to "Uzman",
        6 to "Usta Telsizci",
        7 to "Telsiz Şampiyonu"
    )

    // ── Rozet Koşulları ─────────────────────────────────────

    val ROZET_KOSULLARI: Map<String, (KullaniciIlerleme) -> Boolean> = mapOf(
        "ilk_adim" to { data ->
            (data.kullanici.tamamlananDersler) >= 1
        },
        "nato_caylagi" to { data ->
            data.bolumler["bolum_1"]?.altBolumler?.get("1_1")?.tamamlananDersler?.isNotEmpty() == true
        },
        "nato_ustasi" to { data ->
            data.bolumler["bolum_1"]?.durum == "tamamlandi"
        },
        "q_kodu_bilgesi" to { data ->
            data.bolumler["bolum_2"]?.durum == "tamamlandi"
        },
        "elektronik_dehasi" to { data ->
            data.bolumler["bolum_3"]?.durum == "tamamlandi"
        },
        "frekans_avcisi" to { data ->
            data.bolumler["bolum_4"]?.durum == "tamamlandi"
        },
        "prosedur_ustasi" to { data ->
            data.bolumler["bolum_5"]?.durum == "tamamlandi"
        },
        "7_gun_seri" to { data ->
            data.kullanici.streak >= 7
        },
        "30_gun_seri" to { data ->
            data.kullanici.streak >= 30
        },
        "mukemmeliyetci" to { data ->
            data.kullanici.artArdaHatasiz >= 5
        },
        "trac_adayi" to { data ->
            listOf("bolum_1", "bolum_2", "bolum_3", "bolum_4", "bolum_5", "bolum_6").all { bId ->
                data.bolumler[bId]?.durum == "tamamlandi"
            }
        }
    )

    // ── Rozet Bilgileri ───────────────────────────────────────

    val ROZET_BILGILERI: Map<String, Pair<String, String>> = mapOf(
        "ilk_adim"         to Pair("👣", "İlk dersini tamamladın!"),
        "nato_caylagi"     to Pair("🔤", "İlk NATO dersini tamamladın!"),
        "nato_ustasi"      to Pair("🔤", "Tüm NATO bölümünü geçtin!"),
        "q_kodu_bilgesi"   to Pair("📡", "Tüm Q kodlarını öğrendin!"),
        "elektronik_dehasi" to Pair("⚡", "Elektronik bölümünü tamamladın!"),
        "frekans_avcisi"   to Pair("📻", "Frekans ve Bantlar bölümünü geçtin!"),
        "prosedur_ustasi"  to Pair("🎙️", "Telsiz prosedürlerini öğrendin!"),
        "7_gun_seri"       to Pair("🔥", "7 gün üst üste çalıştın!"),
        "30_gun_seri"      to Pair("🔥", "30 gün üst üste çalıştın!"),
        "mukemmeliyetci"   to Pair("⭐", "5 dersi üst üste hatasız bitirdin!"),
        "trac_adayi"       to Pair("🏆", "Tüm bölümleri tamamladın!")
    )

    fun getRozetIsim(rozetId: String): String = when (rozetId) {
        "ilk_adim"          -> "İlk Adım"
        "nato_caylagi"      -> "NATO Çaylağı"
        "nato_ustasi"       -> "NATO Ustası"
        "q_kodu_bilgesi"    -> "Q Kodu Bilgesi"
        "elektronik_dehasi" -> "Elektronik Dehası"
        "frekans_avcisi"    -> "Frekans Avcısı"
        "prosedur_ustasi"   -> "Prosedür Ustası"
        "7_gun_seri"        -> "7 Günlük Seri"
        "30_gun_seri"       -> "30 Günlük Seri"
        "mukemmeliyetci"    -> "Mükemmeliyetçi"
        "trac_adayi"        -> "TRAC Adayı"
        else                -> "Rozet"
    }

    /**
     * Yeni kazanılan rozetleri kontrol et ve listesini döndür.
     */
    fun kontrolEt(data: KullaniciIlerleme): List<String> {
        val kazanilanlar = data.kullanici.rozetler
        return ROZET_KOSULLARI.entries
            .filter { (id, kosul) -> id !in kazanilanlar && kosul(data) }
            .map { it.key }
    }

    /**
     * Bölüm ilerleme yüzdesini hesapla
     */
    fun getBolumIlerlemeYuzdesi(ilerleme: KullaniciIlerleme, bolumId: String): Int {
        val bolum = ilerleme.bolumler[bolumId] ?: return 0
        val altBolumler = bolum.altBolumler.values.toList()
        if (altBolumler.isEmpty()) return 0

        val tamamlanan = altBolumler.count { it.durum == "tamamlandi" }
        val gecmeSinaviTamamlandi = bolum.gecmeSinavi.durum == "tamamlandi"
        val toplam = altBolumler.size + 1
        val tamamlananToplam = tamamlanan + (if (gecmeSinaviTamamlandi) 1 else 0)

        return ((tamamlananToplam.toFloat() / toplam) * 100).toInt()
    }
}
