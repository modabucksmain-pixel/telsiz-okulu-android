package com.telsizokulu.app.data.model

/**
 * Kullanıcı ilerleme modeli — web'deki localStorage state'ine karşılık gelir.
 * DataStore (Preferences) üzerinden JSON olarak serileştirilir.
 */
data class KullaniciIlerleme(
    val kullanici: KullaniciData = KullaniciData(),
    val bolumler: Map<String, BolumIlerleme> = baslangicBolumDurumu(),
    val kartlar: Map<String, KartDurum> = emptyMap()
)

data class KullaniciData(
    val xp: Int = 0,
    val seviye: Int = 1,
    val streak: Int = 0,
    val enYuksekStreak: Int = 0,
    val sonGiris: String = "",
    val rozetler: List<String> = emptyList(),
    val tamamlananDersler: Int = 0,
    val artArdaHatasiz: Int = 0,
    val gunlukDers: Int = 0,
    val gunlukDersTarih: String = ""
)

data class BolumIlerleme(
    val durum: String = "kilitli",    // "kilitli" | "devam_ediyor" | "tamamlandi"
    val altBolumler: Map<String, AltBolumIlerleme> = emptyMap(),
    val gecmeSinavi: SinavIlerleme = SinavIlerleme()
)

data class AltBolumIlerleme(
    val durum: String = "kilitli",    // "kilitli" | "devam_ediyor" | "tamamlandi"
    val puan: Int? = null,
    val tamamlananDersler: List<Int> = emptyList()
)

data class SinavIlerleme(
    val durum: String = "kilitli",    // "kilitli" | "acik" | "tamamlandi"
    val puan: Int? = null
)

data class KartDurum(
    val durum: String = "ogrenmedim",  // "ogrenmedim" | "ogreniyor" | "ogrendim"
    val dogru: Int = 0,
    val yanlis: Int = 0,
    val artArdaDogru: Int = 0,
    val sonGorulme: String? = null
)

/**
 * Başlangıç bölüm kilit durumu.
 * Bolum_1 açık, diğerleri kilitli.
 */
fun baslangicBolumDurumu(): Map<String, BolumIlerleme> = mapOf(
    "bolum_1" to BolumIlerleme(
        durum = "devam_ediyor",
        altBolumler = mapOf(
            "1_1" to AltBolumIlerleme(durum = "devam_ediyor"),
            "1_2" to AltBolumIlerleme(durum = "kilitli"),
            "1_3" to AltBolumIlerleme(durum = "kilitli"),
            "1_4" to AltBolumIlerleme(durum = "kilitli")
        ),
        gecmeSinavi = SinavIlerleme(durum = "kilitli")
    ),
    "bolum_2" to BolumIlerleme(
        durum = "kilitli",
        altBolumler = mapOf(
            "2_1" to AltBolumIlerleme(), "2_2" to AltBolumIlerleme(),
            "2_3" to AltBolumIlerleme(), "2_4" to AltBolumIlerleme()
        )
    ),
    "bolum_3" to BolumIlerleme(
        durum = "kilitli",
        altBolumler = mapOf(
            "3_1" to AltBolumIlerleme(), "3_2" to AltBolumIlerleme(),
            "3_3" to AltBolumIlerleme(), "3_4" to AltBolumIlerleme()
        )
    ),
    "bolum_4" to BolumIlerleme(
        durum = "kilitli",
        altBolumler = mapOf(
            "4_1" to AltBolumIlerleme(), "4_2" to AltBolumIlerleme(),
            "4_3" to AltBolumIlerleme(), "4_4" to AltBolumIlerleme()
        )
    ),
    "bolum_5" to BolumIlerleme(
        durum = "kilitli",
        altBolumler = mapOf(
            "5_1" to AltBolumIlerleme(), "5_2" to AltBolumIlerleme(),
            "5_3" to AltBolumIlerleme(), "5_4" to AltBolumIlerleme()
        )
    ),
    "bolum_6" to BolumIlerleme(
        durum = "kilitli",
        altBolumler = mapOf(
            "6_1" to AltBolumIlerleme(), "6_2" to AltBolumIlerleme(),
            "6_3" to AltBolumIlerleme(), "6_4" to AltBolumIlerleme()
        )
    )
)
