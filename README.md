# Telsiz Okulu

Türk amatör telsiz operatörü sınav hazırlık uygulaması. NATO fonetik alfabe, Q kodları, elektronik, frekans bantları ve telsiz prosedürlerini kapsayan oyunlaştırılmış kart + egzersiz sistemi.

## İndir

[Son APK → Releases](../../releases/latest)

## Özellikler

- **Bölüm haritası** — sıralı kilit açma, XP + seri sistemi
- **Ders modu** — teori flip kartları → egzersizler
- **Sınavlar** — alt bölüm, bölüm ve genel TRAC sınav modları
- **Pratik** — yanlış yapılan kartlara ağırlıklı alıştırma
- **Kütüphane** — öğrenme durumuna göre tüm kartlara göz at
- **Rozetler** — 20+ başarı rozeti

## Teknoloji

| | |
|---|---|
| Dil | Kotlin 2.1.21 |
| UI | Jetpack Compose + Material Design 3 |
| Mimari | MVVM + StateFlow |
| Veri Kalıcılığı | Jetpack DataStore |
| İçerik | JSON assets (ağ bağlantısı yok) |
| Min SDK | Android 8.0 (API 26) |

## Derleme

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

JDK 17+ gerektirir (JDK 25 ile test edildi). C: diski doluysa `GRADLE_USER_HOME` ortam değişkenini başka bir diske ayarla.

## İçerik Dosyaları

`app/src/main/assets/data/` dizininde:
- `curriculum.json` — bölüm/ders yapısı
- `nato.json`, `qcodes.json`, `elektronik.json`, `bantlar.json`, `prosedurler.json` — kart + egzersizler
- `sinav_sorulari.json` — genel sınav havuzu
- `rozetler.json` — rozet tanımları

## Ses Dosyaları

`app/src/main/assets/sounds/` dizinine ekle (isteğe bağlı):
`squelch_open.wav`, `squelch_close.wav`, `roger_beep.wav`, `dogru.wav`, `yanlis.wav`, `bolum_tamamlandi.wav`

NATO ses dosyaları (`alpha.mp3` vb.) → `app/src/main/assets/audio/nato/`
