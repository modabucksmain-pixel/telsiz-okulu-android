# 📻 Telsiz Okulu

> Türk amatör telsiz (TRAC / HAREC) operatör sınavına hazırlık için oyunlaştırılmış, tamamen çevrimdışı Android uygulaması.

<p align="center">
  <img alt="Sürüm" src="https://img.shields.io/github/v/release/modabucksmain-pixel/telsiz-okulu-android?label=s%C3%BCr%C3%BCm&color=2563EB">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android%208.0%2B-34D399">
  <img alt="Dil" src="https://img.shields.io/badge/Kotlin-2.0-7C3AED">
  <img alt="UI" src="https://img.shields.io/badge/Jetpack%20Compose-M3-38BDF8">
  <img alt="Lisans" src="https://img.shields.io/badge/lisans-MIT-FBBF24">
</p>

NATO fonetik alfabe, Q kodları, elektronik, frekans bantları ve telsiz prosedürlerini kart + egzersiz akışıyla öğreten, XP/seri/rozet sistemiyle desteklenmiş bir eğitim uygulaması. **Ağ bağlantısı gerektirmez** — tüm içerik cihazda.

## ⬇️ İndir

**[Son APK → Releases](../../releases/latest)**

APK'yı indir, telefonda "bilinmeyen kaynaklara izin ver" açıkken kur (sideload).

## ✨ Özellikler

- **🗺️ Bölüm haritası** — sıralı kilit açma; alt bölüm sınavı geçilmeden sonraki bölüm açılmaz
- **📇 Ders modu** — önce teori flip kartları (sesli), sonra alıştırmalar
- **🎯 Sınavlar** — alt bölüm, bölüm ve 50 soruluk genel TRAC sınavı
- **🔁 Pratik** — yanlış yapılan kartlara ağırlıklı tekrar (basit SRS)
- **📚 Kütüphane** — öğrenme durumuna göre tüm kartlara göz at
- **🏅 Rozetler** — 20+ başarı rozeti, neon vektör ikonlar
- **🔊 Gerçekçi telsiz sesi** — squelch cızırtısı, erkek TTS, rastgele parazit, "du-du-dii" onay melodisi (hepsi kodla sentezlenir, ses dosyası gerekmez)
- **🎨 Vektör görseller** — Q kodu, çağrı işareti, elektronik elemanları, anten, bant ve prosedür sorularına özel SVG illüstrasyonlar
- **👋 İlk açılış öğreticisi** — yeni kullanıcıya 4 adımlık tanıtım

## 🧩 Soru Tipleri

Çoktan seçmeli · doğru/yanlış · boşluk doldur · sayısal hesap · dinle & seç · eşleştir · sırala · hece/kodlama · hızlı tur · harfi/kodu yaz

## 🛠️ Teknoloji

| | |
|---|---|
| Dil | Kotlin 2.0 |
| UI | Jetpack Compose + Material Design 3 |
| Mimari | MVVM + StateFlow |
| Navigasyon | Compose Navigation |
| Veri Kalıcılığı | Jetpack DataStore (Preferences) |
| İçerik | Gson — `assets/data/` JSON (ağ yok) |
| Ses | AudioTrack PCM sentezi + Android TextToSpeech |
| Görseller | Coil + SVG decoder |
| Min / Hedef SDK | 26 (Android 8.0) / 35 |

## 🚀 Derleme

```bash
# Debug APK
./gradlew :app:assembleDebug

# Cihaza kur
./gradlew :app:installDebug

# Testler
./gradlew :app:testDebug
```

JDK 17+ gerektirir. C: diski doluysa Gradle önbelleğini başka diske al:

```powershell
$env:GRADLE_USER_HOME='E:\.gradle'
./gradlew :app:assembleDebug
```

## 📂 İçerik Dosyaları

`app/src/main/assets/data/` dizininde:

| Dosya | İçerik |
|---|---|
| `curriculum.json` | Bölüm / alt bölüm / ders yapısı |
| `nato.json` | NATO fonetik alfabe kart + egzersizleri |
| `qcodes.json` | Q kodları |
| `elektronik.json` | Temel elektronik |
| `bantlar.json` | Frekans bantları |
| `prosedurler.json` | Telsiz prosedürleri |
| `sinav_sorulari.json` | Genel sınav havuzu |
| `rozetler.json` | Rozet tanımları |

İçeriği genişletmek için bu JSON dosyalarını düzenlemen yeterli — kod değişikliği gerekmez.

## 🤝 Katkı

Katkılar memnuniyetle karşılanır. Ayrıntı için [CONTRIBUTING.md](CONTRIBUTING.md).

Hızlı yol: soru/kart eklemek istiyorsan ilgili `assets/data/*.json` dosyasını düzenle, derle, test et, PR aç.

## 📄 Lisans

[MIT](LICENSE) — özgürce kullan, değiştir, dağıt.
