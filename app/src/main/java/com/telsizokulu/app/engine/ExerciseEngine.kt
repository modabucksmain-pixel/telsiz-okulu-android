package com.telsizokulu.app.engine

import com.telsizokulu.app.data.model.Egzersiz
import com.telsizokulu.app.data.model.IcerikDosyasi
import com.telsizokulu.app.data.model.KartDurum
import com.telsizokulu.app.data.model.KullaniciIlerleme

/**
 * EgzersizMotoru — Web'deki EgzersizMotoru JS sınıfının Kotlin karşılığı.
 * Ders ve sınav oturumları için soru sıralarını oluşturur ve filtreler.
 */
class ExerciseEngine {

    enum class Mod { DERS, SINAV, PRATIK }
    enum class SinavTipi { ALT_BOLUM, BOLUM, GENEL }

    /**
     * Bir ders oturumu için egzersiz listesi oluştur.
     * Kart ID filtresi + shuffle uygulanır.
     */
    fun dersEgzersizleriOlustur(
        icerik: IcerikDosyasi,
        kartIds: List<String>,
        altBolumId: String,
        egzersizSayisi: Int = 8,
        kartDurumlari: Map<String, KartDurum> = emptyMap()
    ): List<Egzersiz> {
        val tumEgzersizler = mutableListOf<Egzersiz>()

        // Bu ders alt bölümün tüm kartlarını kapsıyor mu? (kapsamlı/karışık ders)
        val altBolumKartSayisi = icerik.kartlar.count { it.altBolum == altBolumId }
        val kapsamliDers = kartIds.isEmpty() || (altBolumKartSayisi > 0 && kartIds.size >= altBolumKartSayisi)

        for ((_, liste) in icerik.egzersizler) {
            val filtrelenmis = liste.filter { egzersiz ->
                when {
                    // kart_id'si olan egzersizler → ders kartlarıyla kısıtla
                    egzersiz.kartId.isNotEmpty() && kartIds.isNotEmpty() -> egzersiz.kartId in kartIds
                    // kart_id'si olmayan tipler (eslestir, kelime_hece, vb.) →
                    // yalnızca kapsamlı derste göster (tüm alt bölüm kartları öğretildiyse)
                    else -> egzersiz.altBolum == altBolumId && kapsamliDers
                }
            }
            tumEgzersizler.addAll(filtrelenmis)
        }

        val rescoped = tumEgzersizler.map { e ->
            if (e.tip == "dinle_sec" && kartIds.isNotEmpty()) {
                val dogru = icerik.kartlar.find { it.id == e.kartId }?.on ?: e.cevap
                var distractors = icerik.kartlar
                    .filter { it.id != e.kartId && it.id in kartIds && !it.on.isNullOrEmpty() }
                    .shuffled().mapNotNull { it.on }
                if (distractors.size < 3) {
                    distractors = icerik.kartlar
                        .filter { it.id != e.kartId && !it.on.isNullOrEmpty() &&
                            (e.altBolum.isEmpty() || it.altBolum == e.altBolum) }
                        .shuffled().mapNotNull { it.on }
                }
                e.copy(secenekler = (listOf(dogru) + distractors.take(3)).shuffled())
            } else e
        }
        val sirali = if (kartDurumlari.isNotEmpty()) agirlikliKaristir(rescoped, kartDurumlari)
                     else rescoped.shuffled()
        return sirali.take(egzersizSayisi)
    }

    /**
     * Sınav oturumu için egzersiz listesi oluştur.
     */
    fun sinavEgzersizleriOlustur(
        icerik: IcerikDosyasi,
        altBolumId: String = "",
        soruSayisi: Int = 20,
        kartDurumlari: Map<String, KartDurum> = emptyMap()
    ): List<Egzersiz> {
        val tumEgzersizler = mutableListOf<Egzersiz>()

        for ((_, liste) in icerik.egzersizler) {
            val filtrelenmis = if (altBolumId.isNotEmpty()) {
                liste.filter { it.altBolum == altBolumId }
            } else {
                liste
            }
            tumEgzersizler.addAll(filtrelenmis)
        }

        val sirali = if (kartDurumlari.isNotEmpty()) agirlikliKaristir(tumEgzersizler, kartDurumlari)
                     else tumEgzersizler.shuffled()
        return sirali.take(soruSayisi)
    }

    /**
     * Zayıflık pratiği için egzersiz listesi oluştur.
     * Kullanıcının en çok yanlış yaptığı konuları öne çıkarır.
     */
    fun pratikEgzersizleriOlustur(
        icerik: IcerikDosyasi,
        kartDurumlari: Map<String, KartDurum>,
        egzersizSayisi: Int = 10
    ): List<Egzersiz> {
        val zayifKartIds = kartDurumlari.entries
            .filter { (_, durum) -> durum.yanlis > 0 && durum.durum != "ogrendim" }
            .sortedByDescending { (_, durum) -> durum.yanlis }
            .map { it.key }
            .take(20)

        if (zayifKartIds.isEmpty()) return emptyList()

        val ilgiliEgzersizler = mutableListOf<Egzersiz>()
        for ((_, liste) in icerik.egzersizler) {
            val filtrelenmis = liste.filter { it.kartId in zayifKartIds }
            ilgiliEgzersizler.addAll(filtrelenmis)
        }

        return agirlikliKaristir(ilgiliEgzersizler, kartDurumlari).take(egzersizSayisi)
    }

    private fun agirlikliKaristir(
        egzersizler: List<Egzersiz>,
        kartDurumlari: Map<String, KartDurum>
    ): List<Egzersiz> {
        fun agirlik(e: Egzersiz): Int = when (kartDurumlari[e.kartId]?.durum) {
            "ogrendim"   -> 1
            "ogreniyor"  -> 3
            "ogrenmedim" -> 5
            else         -> 4
        }
        return egzersizler
            .flatMap { e -> List(agirlik(e)) { e } }
            .shuffled()
            .distinctBy { it.id }
    }

    /**
     * Sınav puanı hesapla
     */
    fun puanHesapla(dogruSayisi: Int, toplamSoru: Int): Int {
        if (toplamSoru == 0) return 0
        return ((dogruSayisi.toFloat() / toplamSoru) * 100).toInt()
    }

    /**
     * Yıldız sayısını hesapla (ders sonuç ekranı için)
     */
    fun yildizHesapla(dogruSayisi: Int, toplamSoru: Int): Int {
        val puan = puanHesapla(dogruSayisi, toplamSoru)
        return when {
            puan >= 90 -> 3
            puan >= 70 -> 2
            puan >= 50 -> 1
            else -> 0
        }
    }
}

/**
 * Tek bir oturum (ders veya sınav) için durum modeli.
 */
data class EgzersizOturumu(
    val egzersizler: List<Egzersiz>,
    val mevcutIndex: Int = 0,
    val dogruSayisi: Int = 0,
    val yanlisSayisi: Int = 0,
    val tamamlandi: Boolean = false,
    val geribildirımGoruntu: Boolean = false,
    val sonCevapDogru: Boolean? = null
) {
    val mevcutEgzersiz: Egzersiz? get() = egzersizler.getOrNull(mevcutIndex)
    val ilerlemeYuzdesi: Float get() = if (egzersizler.isEmpty()) 0f
        else mevcutIndex.toFloat() / egzersizler.size
    val puan: Int get() {
        val toplam = dogruSayisi + yanlisSayisi
        return if (toplam == 0) 0 else ((dogruSayisi.toFloat() / toplam) * 100).toInt()
    }
}
