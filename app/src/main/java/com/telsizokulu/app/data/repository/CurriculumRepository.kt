package com.telsizokulu.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.telsizokulu.app.data.model.Curriculum
import com.telsizokulu.app.data.model.Egzersiz
import com.telsizokulu.app.data.model.IcerikDosyasi
import com.telsizokulu.app.data.model.Kart
import com.telsizokulu.app.data.model.RozetDosyasi

/**
 * Müfredat ve ders içeriklerini assets/ klasöründen okur.
 * Tüm JSON dosyaları Android assets klasöründe saklanır.
 */
class CurriculumRepository(private val context: Context) {

    private val gson = Gson()

    /** Ana müfredat yapısını (curriculum.json) yükle */
    fun loadCurriculum(): Curriculum {
        return loadJson("data/curriculum.json", Curriculum::class.java)
            ?: Curriculum()
    }

    /** Belirli bir bölümün içerik verilerini (nato.json vb.) yükle */
    fun loadIcerik(dosyaAdi: String): IcerikDosyasi {
        val raw = loadJson("data/$dosyaAdi", IcerikDosyasi::class.java) ?: return IcerikDosyasi()
        return raw.copy(
            kartlar = raw.kartlar.map { it.copy(ses = normalizeSes(it.ses)) },
            egzersizler = injectTipVeOnarim(raw)
        )
    }

    /**
     * Egzersiz map key'ini (tip) her egzersize enjekte eder + tip-spesifik alan eksikliklerini onarır.
     * Gson non-null String alanların default değerlerini bypass ettiği için bu adım zorunlu.
     */
    private fun injectTipVeOnarim(raw: IcerikDosyasi): Map<String, List<Egzersiz>> {
        return raw.egzersizler.mapValues { (tipKey, liste) ->
            liste.map { e ->
                var fixed = e

                // 0. Gson null bypass: tüm String alanları güvenli hale getir
                // Gson non-null String fields'ları null bırakır; cast-to-nullable ile temizle
                @Suppress("SENSELESS_COMPARISON")
                fixed = fixed.copy(
                    id         = (fixed.id as? String)        ?: "",
                    tip        = (fixed.tip as? String)       ?: "",
                    soru       = (fixed.soru as? String)      ?: "",
                    metin      = (fixed.metin as? String)     ?: "",
                    cevap      = (fixed.cevap as? String)     ?: "",
                    aciklama   = (fixed.aciklama as? String)  ?: "",
                    birim      = (fixed.birim as? String)     ?: "",
                    gorsel     = (fixed.gorsel as? String)    ?: "",
                    kartId     = (fixed.kartId as? String)    ?: "",
                    altBolum   = (fixed.altBolum as? String)  ?: "",
                    zorluk     = (fixed.zorluk as? String)    ?: "orta",
                    dogruKart  = (fixed.dogruKart as? String) ?: "",
                    gosterilen = (fixed.gosterilen as? String)?: "",
                    kelime     = (fixed.kelime as? String)    ?: "",
                    secenekler = fixed.secenekler ?: emptyList(),
                    adimlar    = fixed.adimlar    ?: emptyList(),
                    cifler     = fixed.cifler     ?: emptyList(),
                    sorular    = fixed.sorular    ?: emptyList()
                )

                // 1. tip null/empty → map key'den al
                if (fixed.tip.isEmpty()) {
                    fixed = fixed.copy(tip = tipKey)
                }

                // 2. dinle_sec: dogru_kart'tan cevap + kartId + secenekler üret
                if (fixed.tip == "dinle_sec" && fixed.dogruKart.isNotEmpty() && fixed.secenekler.isEmpty()) {
                    val dogru = raw.kartlar.find { it.id == fixed.dogruKart }?.on ?: ""
                    val distracters = raw.kartlar
                        .filter {
                            it.id != fixed.dogruKart &&
                            !it.on.isNullOrEmpty() &&
                            (fixed.altBolum.isEmpty() || it.altBolum == fixed.altBolum)
                        }
                        .shuffled().take(3).mapNotNull { it.on }
                    fixed = fixed.copy(
                        cevap = dogru,
                        kartId = fixed.dogruKart,
                        secenekler = (listOf(dogru) + distracters).shuffled()
                    )
                }

                // 3. harfi_yaz / kodu_yaz: gosterilen'i soru olarak kullan (soru generic tekst)
                if ((fixed.tip == "harfi_yaz" || fixed.tip == "kodu_yaz") && fixed.gosterilen.isNotEmpty()) {
                    fixed = fixed.copy(soru = fixed.gosterilen)
                }

                // 4. kelime_hece: kelime'yi soru olarak kullan
                if (fixed.tip == "kelime_hece" && fixed.kelime.isNotEmpty()) {
                    fixed = fixed.copy(soru = fixed.kelime)
                }

                fixed
            }
        }
    }

    /** Web path'ini Android asset adına dönüştür: /static/audio/nato/alpha.mp3 → alpha */
    private fun normalizeSes(ses: String?): String {
        if (ses.isNullOrEmpty()) return ""
        return ses.substringAfterLast('/').substringBeforeLast('.')
    }

    /** Rozet tanımlarını yükle */
    fun loadRozetler(): RozetDosyasi {
        return loadJson("data/rozetler.json", RozetDosyasi::class.java)
            ?: RozetDosyasi()
    }

    /** Tüm izinli dosyaların içeriklerini birleştirerek döndür */
    fun loadTumIcerik(): IcerikDosyasi {
        val izinliDosyalar = listOf(
            "nato.json", "qcodes.json", "elektronik.json",
            "bantlar.json", "prosedurler.json", "sinav_sorulari.json"
        )
        val tumKartlar = mutableListOf<Kart>()
        val tumEgzersizler = mutableMapOf<String, MutableList<Egzersiz>>()

        for (dosya in izinliDosyalar) {
            val icerik = loadIcerik(dosya)
            tumKartlar.addAll(icerik.kartlar)
            for ((tip, liste) in icerik.egzersizler) {
                tumEgzersizler.getOrPut(tip) { mutableListOf() }.addAll(liste)
            }
        }

        return IcerikDosyasi(
            baslik = "Tüm İçerik",
            kartlar = tumKartlar,
            egzersizler = tumEgzersizler
        )
    }

    /** Belirli kart ID'leri ile eşleşen içerikleri yükle */
    fun loadIcerikForKartIds(kartIds: List<String>, veriDosyasi: String): IcerikDosyasi {
        val icerik = loadIcerik(veriDosyasi)
        val filtreliKartlar = icerik.kartlar.filter { it.id in kartIds }
        return icerik.copy(kartlar = filtreliKartlar)
    }

    private fun <T> loadJson(path: String, clazz: Class<T>): T? {
        return try {
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            gson.fromJson(json, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
