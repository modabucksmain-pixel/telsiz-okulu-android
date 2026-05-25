package com.telsizokulu.app.data.model

import com.google.gson.annotations.SerializedName

// ── Curriculum Modelleri ──────────────────────────────────────────────────

data class Curriculum(
    @SerializedName("bolumler") val bolumler: List<Bolum> = emptyList()
)

data class Bolum(
    @SerializedName("id") val id: String,
    @SerializedName("baslik") val baslik: String,
    @SerializedName("ikon") val ikon: String,
    @SerializedName("renk") val renk: String,
    @SerializedName("aciklama") val aciklama: String,
    @SerializedName("veri_dosyasi") val veriDosyasi: String,
    @SerializedName("siralama") val siralama: Int,
    @SerializedName("alt_bolumler") val altBolumler: List<AltBolum> = emptyList(),
    @SerializedName("gecme_sinavi") val gecmeSinavi: SinavKonfig = SinavKonfig()
)

data class AltBolum(
    @SerializedName("id") val id: String,
    @SerializedName("baslik") val baslik: String,
    @SerializedName("aciklama") val aciklama: String,
    @SerializedName("harfler") val harfler: List<String> = emptyList(),
    @SerializedName("dersler") val dersler: List<Ders> = emptyList(),
    @SerializedName("sinav") val sinav: SinavKonfig = SinavKonfig()
)

data class Ders(
    @SerializedName("no") val no: Int,
    @SerializedName("baslik") val baslik: String,
    @SerializedName("egzersiz_sayisi") val egzersizSayisi: Int = 8,
    @SerializedName("kartlar") val kartlar: List<String> = emptyList()
)

data class SinavKonfig(
    @SerializedName("soru_sayisi") val soruSayisi: Int = 15,
    @SerializedName("gecme_puani") val gecmePuani: Int = 60
)

// ── İçerik Modelleri (nato.json, qcodes.json, vb.) ───────────────────────

data class IcerikDosyasi(
    @SerializedName("baslik") val baslik: String = "",
    @SerializedName("kartlar") val kartlar: List<Kart> = emptyList(),
    @SerializedName("egzersizler") val egzersizler: Map<String, List<Egzersiz>> = emptyMap()
)

data class Kart(
    @SerializedName("id") val id: String,
    @SerializedName(value = "on", alternate = ["harf", "kavram", "terim", "soru", "baslik"]) val on: String?,
    @SerializedName(value = "arka", alternate = ["kod", "anlam", "cevap", "metin"]) val arka: String?,
    @SerializedName("ikon") val ikon: String? = "",
    @SerializedName("ses") val ses: String? = "",
    @SerializedName("telaffuz") val telaffuz: String? = "",
    @SerializedName("aciklama") val aciklama: String? = "",
    @SerializedName("kategori") val kategori: String? = "",
    @SerializedName("alt_bolum") val altBolum: String? = "",
    @SerializedName("etiketler") val etiketler: List<String> = emptyList()
)

data class Egzersiz(
    @SerializedName("id") val id: String = "",
    @SerializedName("tip") val tip: String = "",   // Gson bypasses default; CurriculumRepository injects from map key
    @SerializedName("soru") val soru: String = "",
    @SerializedName("metin") val metin: String = "",
    @SerializedName(value = "cevap", alternate = ["dogru", "beklenen", "dogru_sececnek"]) val cevap: String = "",
    @SerializedName("secenekler") val secenekler: List<String> = emptyList(),
    @SerializedName(value = "kart_id", alternate = ["kart"]) val kartId: String = "",
    @SerializedName("alt_bolum") val altBolum: String = "",
    @SerializedName("zorluk") val zorluk: String = "orta",
    @SerializedName("aciklama") val aciklama: String = "",
    @SerializedName("birim") val birim: String = "",
    @SerializedName("gorsel") val gorsel: String = "",
    @SerializedName("diyalog") val diyalog: List<DiyalogSatiri>? = null,
    @SerializedName("adimlar") val adimlar: List<String> = emptyList(),
    @SerializedName("cifler") val cifler: List<EslestirmeCifti> = emptyList(),
    @SerializedName("sorular") val sorular: List<HizliTurSoruTanim> = emptyList(),
    @SerializedName("sure") val sure: Int = 10,
    // Fields used by specific exercise types with non-standard JSON names:
    @SerializedName("dogru_kart") val dogruKart: String = "",   // dinle_sec
    @SerializedName("gosterilen") val gosterilen: String = "",  // harfi_yaz, kodu_yaz
    @SerializedName("kelime") val kelime: String = "",          // kelime_hece
)

data class HizliTurSoruTanim(
    @SerializedName("gosterilen") val gosterilen: String = "",
    @SerializedName("dogru") val dogru: String = "",
    @SerializedName("secenekler") val secenekler: List<String> = emptyList()
)


data class DiyalogSatiri(
    @SerializedName("kisi") val kisi: String = "",
    @SerializedName("metin") val metin: String = "",
    @SerializedName("taraf") val taraf: String = "verici"
)

data class EslestirmeCifti(
    @SerializedName("sol") val sol: String = "",
    @SerializedName("sag") val sag: String = ""
)

// ── Rozet Modeli ─────────────────────────────────────────────────────────

data class RozetDosyasi(
    @SerializedName("rozetler") val rozetler: List<RozetTanim> = emptyList()
)

data class RozetTanim(
    @SerializedName("id") val id: String,
    @SerializedName("ikon") val ikon: String,
    @SerializedName("isim") val isim: String,
    @SerializedName("aciklama") val aciklama: String
)
