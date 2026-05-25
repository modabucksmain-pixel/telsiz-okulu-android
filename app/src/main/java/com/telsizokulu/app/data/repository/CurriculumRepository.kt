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
                telaffuz  = (k.telaffuz as? String) ?: "",
                aciklama  = (k.aciklama as? String) ?: "",
                kategori  = k.kategori,
                altBolum  = k.altBolum,
                etiketler = (k.etiketler as? List<String>) ?: emptyList()
            )
        }
        val egzersizler = injectTipVeOnarim(raw).toMutableMap()

        // NATO + Q + Elektronik + Bant kartları için inandırıcı çoktan seçmeli üret.
        val uretilen = natoSecmeliUret(temizKartlar) +
            cagriIsaretiUret(temizKartlar) +
            qKoduSecmeliUret(temizKartlar) +
            genelKartSecmeliUret(
                temizKartlar, "el_",
                ileriSoru = { on -> "Elektronikte \"$on\" sembolü hangi birimi gösterir?" },
                tersSoru  = { arka -> "\"$arka\" biriminin sembolü hangisidir?" }
            ) +
            genelKartSecmeliUret(
                temizKartlar, "bn_",
                ileriSoru = { on -> "\"$on\" neye karşılık gelir?" },
                tersSoru  = { arka -> "Hangisi \"$arka\" karşılığıdır?" }
            )
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
     * Çağrı işareti okuma soruları üret: "TA2BC" → "Tango Alpha Two Bravo Charlie".
     * NATO kartlarından harf/rakam→kod map kurar. Dağıtıcı: bir kelimesi değiştirilmiş varyant.
     */
    private fun cagriIsaretiUret(kartlar: List<Kart>): List<Egzersiz> {
        val natoMap = kartlar
            .filter { it.id.startsWith("nato_") && !it.on.isNullOrBlank() && !it.arka.isNullOrBlank() }
            .associate { (it.on ?: "").uppercase() to (it.arka ?: "") }
        if (natoMap.size < 10) return emptyList()
        val kodlar = natoMap.values.distinct()

        val cagrilar = listOf("TA2BC", "TA1XY", "TB3KM", "YM7LA", "TA5FG", "TA4QP", "TB2RS", "TA9DK")
        val rnd = java.util.Random()
        val sonuc = mutableListOf<Egzersiz>()

        cagrilar.forEachIndexed { idx, cs ->
            val kelimeler = cs.mapNotNull { ch -> natoMap[ch.toString().uppercase()] }
            if (kelimeler.size != cs.length) return@forEachIndexed
            val dogru = kelimeler.joinToString(" ")

            val distractors = mutableSetOf<String>()
            var guard = 0
            while (distractors.size < 3 && guard < 40) {
                guard++
                val pos = rnd.nextInt(kelimeler.size)
                val yeni = kodlar.filter { it != kelimeler[pos] }.shuffled().first()
                val varyant = kelimeler.toMutableList().also { it[pos] = yeni }.joinToString(" ")
                if (varyant != dogru) distractors.add(varyant)
            }
            if (distractors.size == 3) {
                sonuc.add(Egzersiz(
                    id = "cs_$idx", tip = "coktan_secmeli",
                    soru = "\"$cs\" çağrı işaretini fonetik alfabeyle nasıl okursun?",
                    cevap = dogru,
                    secenekler = (listOf(dogru) + distractors).shuffled(),
                    kartId = "", altBolum = "1_4", zorluk = "zor",
                    aciklama = "$cs = $dogru"
                ))
            }
        }
        return sonuc
    }

    /**
     * Q kodu kartlarından çoktan seçmeli soru üret.
     * Q kodları birbirine çok benzer (QRM/QRN/QRT/QRS) → diğer gerçek kodlar doğal tuzak.
     * Gson alan belirsizliğine karşı kod regex ile tespit edilir, anlam diğer alandan alınır.
     */
    private fun qKoduSecmeliUret(kartlar: List<Kart>): List<Egzersiz> {
        val qRegex = Regex("^Q[A-Z]{2,3}$")
        fun kodMu(s: String?) = s != null && qRegex.matches(s.trim())

        data class QKart(val id: String, val kod: String, val anlam: String, val ab: String)
        val qKartlar = kartlar.filter { it.id.startsWith("q_") }.mapNotNull { k ->
            val kod = listOf(k.on, k.arka).firstOrNull { kodMu(it) }?.trim()
            val anlam = listOf(k.on, k.arka).firstOrNull { !kodMu(it) && !it.isNullOrBlank() }?.trim()
            if (kod != null && anlam != null) QKart(k.id, kod, anlam, k.altBolum ?: "") else null
        }
        if (qKartlar.size < 4) return emptyList()

        val tumKodlar = qKartlar.map { it.kod }.distinct()
        val tumAnlamlar = qKartlar.map { it.anlam }.distinct()
        val sonuc = mutableListOf<Egzersiz>()

        qKartlar.forEach { q ->
            // İleri: anlam → kod (benzer Q kodları tuzak)
            val yanlisKodlar = tumKodlar.filter { it != q.kod }.shuffled().take(3)
            if (yanlisKodlar.size == 3) {
                val secenekler = (listOf(q.kod) + yanlisKodlar).shuffled()
                sonuc.add(Egzersiz(
                    id = "qks_${q.id}", tip = "coktan_secmeli",
                    soru = "\"${q.anlam}\" karşılığı hangi Q kodudur?",
                    cevap = q.kod, secenekler = secenekler,
                    kartId = q.id, altBolum = q.ab, zorluk = "orta",
                    aciklama = "${q.kod} = ${q.anlam}"
                ))
            }
            // Ters: kod → anlam
            val yanlisAnlamlar = tumAnlamlar.filter { it != q.anlam }.shuffled().take(3)
            if (yanlisAnlamlar.size == 3) {
                val secenekler = (listOf(q.anlam) + yanlisAnlamlar).shuffled()
                sonuc.add(Egzersiz(
                    id = "qas_${q.id}", tip = "coktan_secmeli",
                    soru = "\"${q.kod}\" ne anlama gelir?",
                    cevap = q.anlam, secenekler = secenekler,
                    kartId = q.id, altBolum = q.ab, zorluk = "orta",
                    aciklama = "${q.kod} = ${q.anlam}"
                ))
            }
        }
        return sonuc
    }

    /**
     * Genel kart→çoktan seçmeli üretici (elektronik, bant vb.).
     * on (kısa sembol/etiket) ↔ arka (kısa birim/karşılık) iki yönlü MC.
     * Uzun aciklama metni gelirse atlanır (kısa alan filtresi). Dağıtıcılar önce aynı
     * alt bölümden (anlamlı tuzak), yetmezse tüm setten.
     */
    private fun genelKartSecmeliUret(
        kartlar: List<Kart>,
        prefix: String,
        ileriSoru: (on: String) -> String,
        tersSoru: (arka: String) -> String,
        zorluk: String = "orta"
    ): List<Egzersiz> {
        val secili = kartlar.filter {
            it.id.startsWith(prefix) &&
                !it.on.isNullOrBlank() && !it.arka.isNullOrBlank() &&
                (it.on?.length ?: 99) <= 14 && (it.arka?.length ?: 99) <= 44
        }
        if (secili.size < 4) return emptyList()

        val sonuc = mutableListOf<Egzersiz>()

        fun dagitici(secenekKaynak: (Kart) -> String?, k: Kart, dogru: String): List<String> {
            val ayniAb = secili.filter { it.altBolum == k.altBolum && it.id != k.id }
                .mapNotNull(secenekKaynak).filter { it != dogru }.distinct()
            val havuz = if (ayniAb.size >= 3) ayniAb
            else (ayniAb + secili.filter { it.id != k.id }.mapNotNull(secenekKaynak)).filter { it != dogru }.distinct()
            return havuz.shuffled().take(3)
        }

        secili.forEach { k ->
            val on = k.on ?: ""
            val arka = k.arka ?: ""
            val ab = k.altBolum ?: ""

            val yArka = dagitici({ it.arka }, k, arka)
            if (yArka.size == 3) {
                sonuc.add(Egzersiz(
                    id = "${prefix}ks_${k.id}", tip = "coktan_secmeli",
                    soru = ileriSoru(on), cevap = arka,
                    secenekler = (listOf(arka) + yArka).shuffled(),
                    kartId = k.id, altBolum = ab, zorluk = zorluk,
                    aciklama = "$on = $arka"
                ))
            }

            val yOn = dagitici({ it.on }, k, on)
            if (yOn.size == 3) {
                sonuc.add(Egzersiz(
                    id = "${prefix}as_${k.id}", tip = "coktan_secmeli",
                    soru = tersSoru(arka), cevap = on,
                    secenekler = (listOf(on) + yOn).shuffled(),
                    kartId = k.id, altBolum = ab, zorluk = zorluk,
                    aciklama = "$arka = $on"
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
