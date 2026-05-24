# Telsiz Okulu — Native Android Projesi

Bu klasör, Telsiz Okulu web uygulamasının **tamamen Native Android** portudur.
Kotlin + Jetpack Compose + Material Design 3 kullanılarak sıfırdan yazılmıştır.

## Teknoloji Yığını

- **Dil**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Tema**: Premium Koyu Mod (Slate `#0F172A`, Blue `#2563EB`, Purple `#7C3AED`)
- **Veri**: Jetpack DataStore (Preferences) — İlerleme; Assets JSON — Müfredat
- **Ses**: Android SoundPool + TextToSpeech API
- **Navigasyon**: Compose Navigation
- **Mimari**: MVVM (ViewModel + StateFlow)

## Proje Yapısı

```
android_native/
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   ├── data/           ← 8 JSON veri dosyası (NATO, QCode, Elektronik, vb.)
│   │   │   ├── img/svg/        ← 32 SVG ikonu
│   │   │   └── sounds/         ← Telsiz ses efektleri (.wav) — EKLENMELI
│   │   └── java/com/telsizokulu/app/
│   │       ├── MainActivity.kt
│   │       ├── TelsizOkuluApp.kt
│   │       ├── data/
│   │       │   ├── model/      ← Tüm veri sınıfları
│   │       │   └── repository/ ← CurriculumRepository, ProgressRepository
│   │       ├── engine/
│   │       │   ├── AudioEngine.kt      ← SoundPool + TTS
│   │       │   ├── GamificationEngine.kt ← XP, Rozet, Seviye
│   │       │   └── ExerciseEngine.kt   ← Soru seçimi ve filtreleme
│   │       └── ui/
│   │           ├── theme/      ← Color, Type, Theme
│   │           ├── navigation/ ← Screen, AppNavGraph
│   │           ├── screens/    ← Tüm ekranlar
│   │           └── viewmodel/  ← HomeViewModel, ExerciseViewModel, ProfileViewModel
```

## Kurulum

### 1. Android Studio'da Aç
`android_native` klasörünü Android Studio'da aç:
**File → Open → `android_native` klasörünü seç**

### 2. Ses Dosyaları Ekle (Gerekli)
`app/src/main/assets/sounds/` klasörüne şu ses dosyalarını ekle:
- `squelch_open.wav` — Telsiz açma sesi (kısa cızırtı, ~80ms)
- `squelch_close.wav` — Telsiz kapama sesi (~60ms)
- `roger_beep.wav` — Roger Beep 880Hz sine tonu (~110ms)
- `dogru.wav` — Doğru cevap sesi (C5→E5 yükselen ton)
- `yanlis.wav` — Yanlış cevap sesi (düşen ton)
- `bolum_tamamlandi.wav` — Bölüm tamamlama fanfarı

### 3. Derle ve Çalıştır
```
./gradlew :app:assembleDebug
```
veya Android Studio'da `Run` düğmesine bas.

## Ekranlar

| Ekran | Açıklama |
|-------|----------|
| **Ana Sayfa** | Bölüm haritası, XP/Streak/Rozet istatistikleri, Günlük Hedef |
| **Bölüm Detay** | Alt bölüm listesi, sıralı ders kilitleri, Alt Bölüm/Bölüm sınavları |
| **Ders/Egzersiz** | Çoktan Seçmeli, Doğru/Yanlış, Boşluk Doldur, Sayısal sorular |
| **Sınav** | Tüm sınav tipleri (Alt Bölüm, Bölüm, Genel TRAC) |
| **Kütüphane** | Tüm teori kartları ve konu özetleri |
| **Profil** | Seviye ilerleme, Bölüm ilerlemeleri, Rozet vitrini, Öğrenme durumu |

## Web'den Port Edilen Özellikler

| Web Özelliği | Android Karşılığı |
|-------------|------------------|
| `localStorage` (TelsizProgress) | Jetpack DataStore |
| `SesYoneticisi` + `WebAudioSynth` | `AudioEngine` (SoundPool + TTS) |
| `Gamification` JS sınıfı | `GamificationEngine` Kotlin object |
| `EgzersizMotoru` JS sınıfı | `ExerciseEngine` Kotlin class |
| Flask Jinja2 Templates | Jetpack Compose Screens |
| CSS Koyu Mod | Material3 Dark Color Scheme |
