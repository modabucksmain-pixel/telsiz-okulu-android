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
        // Gson, JSON'da olmayan non-null alanlara null enjekte eder; copy() öncesi temizle.
        @Suppress("SENSELESS_COMPARISON", "USELESS_CAST")
        val temizKartlar = (raw.kartlar ?: emptyList()).map { k ->
            Kart(
                id        = (k.id as? String) ?: "",
                on        = k.on,
                arka      = k.arka,
                ikon      = k.ikon,
                ses       = normalizeSes(k.ses),
                kategori  = k.kategori,
                altBolum  = k.altBolum,
                etiketler = (k.etiketler as? List<String>) ?: emptyList()
            )
        }
        val egzersizler = injectTipVeOnarim(raw).toMutableMap()

        // NATO kartları için inandırıcı çoktan seçmeli sorular üret (harf→kod ve kod→harf).
        val uretilen = natoSecmeliUret(temizKartlar)
        if (uretilen.isNotEmpty()) {
            egzersizler["coktan_secmeli"] = (egzersizler["coktan_secmeli"] ?: emptyList()) + uretilen
        }

        return IcerikDosyasi(
            baslik = (raw.baslik as? String) ?: "",
            kartlar = temizKartlar,
            egzersizler = egzersizler
        )
    }

    /**
     * NATO kartlarından çoktan seçmeli soru üret.
     * Dağıtıcılar aynı harfle başlayan, kulağa NATO gibi gelen sahte kelimeler (sinsi tuzak).
     */
    private fun natoSecmeliUret(kartlar: List<Kart>): List<Egzersiz> {
        val natoKartlar = kartlar.filter {
            it.id.startsWith("nato_") && !it.arka.isNullOrEmpty() && !it.on.isNullOrEmpty()
        }
        if (natoKartlar.isEmpty()) return emptyList()

        val tumHarfler = natoKartlar.mapNotNull { it.on?.uppercase() }.distinct()
        val sonuc = mutableListOf<Egzersiz>()

        natoKartlar.forEach { k ->
            val harf = (k.on ?: "").uppercase()
            val kod  = k.arka ?: ""
            val ab   = k.altBolum ?: ""

            // İleri: "B" harfinin kodu? → Bravo / Beta / Bordon / Bistra
            val havuz = NATO_DISTRAKTOR[harf]
            if (!havuz.isNullOrEmpty() && havuz.size >= 3) {
                val secenekler = (listOf(kod) + havuz.shuffled().take(3)).shuffled()
                sonuc.add(Egzersiz(
                    id = "ks_${k.id}", tip = "coktan_secmeli",
                    soru = "\"$harf\" harfinin NATO kodu hangisidir?",
                    cevap = kod, secenekler = secenekler,
                    kartId = k.id, altBolum = ab, zorluk = "kolay",
                    aciklama = "$harf = $kod"
                ))
            }

            // Ters: "Bravo" hangi harf? → diğer harfler dağıtıcı
            val yanlisHarfler = tumHarfler.filter { it != harf }.shuffled().take(3)
            if (yanlisHarfler.size == 3) {
                val secenekler = (listOf(harf) + yanlisHarfler).shuffled()
                sonuc.add(Egzersiz(
                    id = "hs_${k.id}", tip = "coktan_secmeli",
                    soru = "\"$kod\" hangi harfe karşılık gelir?",
                    cevap = harf, secenekler = secenekler,
                    kartId = k.id, altBolum = ab, zorluk = "kolay",
                    aciklama = "$kod = $harf"
                ))
            }
        }
        return sonuc
    }

    /**
     * Egzersiz map key'ini (tip) her egzersize enjekte eder + tip-spesifik alan eksikliklerini onarır.
     * Gson non-null String alanların default değerlerini bypass ettiği için bu adım zorunlu.
     */
    private fun injectTipVeOnarim(raw: IcerikDosyasi): Map<String, List<Egzersiz>> {
        @Suppress("SENSELESS_COMPARISON")
        val rawKartlar = raw.kartlar ?: emptyList()
        @Suppress("SENSELESS_COMPARISON")
        val rawEgzersizler = raw.egzersizler ?: emptyMap()
        return rawEgzersizler.mapValues { (tipKey, liste) ->
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
                    val dogru = rawKartlar.find { it.id == fixed.dogruKart }?.on ?: ""
                    val distracters = rawKartlar
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

    companion object {
        /**
         * NATO çoktan seçmeli dağıtıcı havuzu.
         * Hepsi doğru harfle başlar + kulağa NATO gibi gelir = inandırıcı tuzak.
         * Karışım: sahte-fonetik, yazım varyantı (Alfa/Juliett/Fokstrot), eski/ulusal
         * fonetik (Coca/Boston/Easy/Niner), Türkçe tanıdık (Bursa/Ankara).
         */
        private val NATO_DISTRAKTOR: Map<String, List<String>> = mapOf(
            "A" to listOf("Alfa", "Atlas", "Ankara", "Apollo", "Anna"),
            "B" to listOf("Beta", "Bordon", "Bingo", "Bursa", "Boston"),
            "C" to listOf("Cesar", "Cobra", "Coca", "Carlos", "Ceyhan"),
            "D" to listOf("Denver", "Diana", "Dakota", "Delfin", "Dingo"),
            "E" to listOf("Easy", "Edison", "Ekko", "Empire", "Edirne"),
            "F" to listOf("Falcon", "Fokstrot", "Fiesta", "Fethiye", "Frodo"),
            "G" to listOf("Gamma", "Gallo", "Gustav", "Genova", "Gebze"),
            "H" to listOf("Havana", "Hanover", "Henry", "Hilton", "Hatay"),
            "I" to listOf("Italy", "Indigo", "Isaac", "Iglo", "Igloo"),
            "J" to listOf("Juliett", "Jupiter", "Jersey", "Jumbo", "Java"),
            "K" to listOf("Kilogram", "Kenya", "Kappa", "Karma", "Konya"),
            "L" to listOf("Lambda", "London", "Luna", "Lotus", "Lima Peru"),
            "M" to listOf("Metro", "Mango", "Madrid", "Morse", "Mersin"),
            "N" to listOf("Norway", "Nectar", "Nevada", "Niagara", "Nazar"),
            "O" to listOf("Omega", "Oslo", "Orion", "Otto", "Ordu"),
            "P" to listOf("Pluto", "Panter", "Prag", "Pizza", "Polo"),
            "Q" to listOf("Quito", "Quasar", "Quartz", "Queen", "Quad"),
            "R" to listOf("Radio", "Rocket", "Roma", "Rambo", "Rize"),
            "S" to listOf("Sigma", "Santos", "Saturn", "Sofya", "Samsun"),
            "T" to listOf("Titan", "Tahoe", "Tokyo", "Tonga", "Trabzon"),
            "U" to listOf("Union", "Ural", "Utah", "Ultra", "Uşak"),
            "V" to listOf("Vektor", "Venus", "Vienna", "Volga", "Van"),
            "W" to listOf("Wilson", "Wave", "Winter", "Wagon", "Walter"),
            "X" to listOf("Xenon", "Xander", "Xerox", "Xylo", "Xena"),
            "Y" to listOf("Yale", "Yukon", "Yoga", "Yeti", "Yalova"),
            "Z" to listOf("Zebra", "Zorro", "Zeus", "Zenith", "Zonguldak"),
            // Rakamlar (havacılık varyantları gerçek tuzak: Niner/Fife/Tree)
            "0" to listOf("Ziro", "Nought", "Null", "Nil"),
            "1" to listOf("Won", "Uno", "Wun", "Ein"),
            "2" to listOf("Too", "Twin", "Duo", "Tu"),
            "3" to listOf("Tree", "Tri", "Free", "Thri"),
            "4" to listOf("For", "Fower", "Quad", "Fawer"),
            "5" to listOf("Fife", "Penta", "Faiv", "Quint"),
            "6" to listOf("Sics", "Hex", "Saks", "Sixer"),
            "7" to listOf("Sevn", "Hepta", "Sefen", "Sven"),
            "8" to listOf("Ait", "Octa", "Eit", "Ate"),
            "9" to listOf("Niner", "Nain", "Nona", "Nyne")
        )
    }
}
