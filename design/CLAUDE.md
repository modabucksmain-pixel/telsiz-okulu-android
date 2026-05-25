# CLAUDE.md — Android Port Talimatı

> Bu dosya **Claude Code** içindir.
> Görev: `Telsiz Okulu.html` HTML prototipini **Kotlin + Jetpack Compose** Android uygulamasına 1:1 port etmek.
> Tasarım kaynağı: bu klasördeki HTML/JSX dosyaları **tek doğruluk kaynağıdır** (single source of truth).
> Pixel-perfect değil — **token-perfect** hedeflenir: renk/spacing/tipografi token'ları birebir, layout yapısı birebir, etkileşim akışları birebir.

---

## 0 · Görev özeti

Sıfırdan veya mevcut iskelet üzerine bir Android uygulaması üret:

- **Stack:** Kotlin · Jetpack Compose (Material 3) · Navigation-Compose · DataStore · Hilt (opsiyonel)
- **Min SDK:** 26 · **Target/Compile SDK:** 34
- **Tek aktivite** + Compose nav.
- **Tüm UI Türkçe** (string'leri `strings.xml`'e taşı, ama metin Türkçe).
- **Edge-to-edge** + `WindowCompat.setDecorFitsSystemWindows(false)`.
- **Karanlık mod varsayılan** (Spektrum teması).

Bittiğinde uygulama; HTML prototipinde olan **7 ekranı**, **6 bölüm × 5 dersi**, **Grill Me FAB**, **bottom nav**, **kütüphane sekmelerini** ve **profil + rozetleri** içermeli.

---

## 1 · Kaynak dosyalar (sırasıyla oku)

Aşağıdaki dosyaları **bu sırayla** oku, içselleştir, ondan sonra koda başla:

| # | Dosya | İçindeki |
|---|---|---|
| 1 | `README.md` | Genel konsept, ekran şemaları, akışlar (Türkçe) |
| 2 | `data.js` | 6 bölüm × 5 ders müfredatı, NATO, Q-kodu, mors, rozet listeleri |
| 3 | `theme.js` | Tema token üreteci — 3 varyant × 3 aksent |
| 4 | `Telsiz Okulu.html` | Script yükleme sırası, font setup |
| 5 | `home.jsx` | Ana sayfa: top bar, bölüm seçici, devam butonu, ders listesi, FAB |
| 6 | `chapter-pages.jsx` | 6 bölüme özel showcase widget'ı |
| 7 | `screens.jsx` | Bölüm detay, ders/sınav akışı, kütüphane, profil, pratik |
| 8 | `app.jsx` | Router, state reducer, bottom nav, Grill Me modal |
| 9 | `mascot.jsx` | Maskot (varsa) |

> **Önemli:** `tweaks-panel.jsx` ve `design_handoff_ana_menu/` **port edilmez**. Tweaks bir tasarım explorer aracıdır; üretim uygulamasında bulunmaz.

---

## 2 · Tasarım token'ları (Compose)

Bu hex değerleri **birebir** kullan. Tahmin etme.

### 2.1 Renk şeması — Spektrum (varsayılan, dark)

```kotlin
// ui/theme/Color.kt
val SpektrumColors = darkColorScheme(
    primary          = Color(0xFF5BD9C8),  // teal-cyan aksent
    onPrimary        = Color(0xFF0B1020),
    primaryContainer = Color(0xFF14302D),  // accentTint
    secondary        = Color(0xFFFFB454),  // streak / FAB
    background       = Color(0xFF0B1020),
    onBackground     = Color(0xFFF5F2E9),
    surface          = Color(0xFF11172A),
    surfaceVariant   = Color(0xFF0E1426),
    onSurface        = Color(0xFFF5F2E9),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline          = Color(0xFF1F2937),
    outlineVariant   = Color(0xFF2C3849),
    error            = Color(0xFFB91C1C),
)
```

### 2.2 Renk şeması — Sade (light, opsiyonel)

```kotlin
val SadeColors = lightColorScheme(
    primary          = Color(0xFF1E3A8A),
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8ECF7),
    secondary        = Color(0xFFDC6803),
    background       = Color(0xFFF5F2E9),
    onBackground     = Color(0xFF0B1020),
    surface          = Color(0xFFFFFFFF),
    surfaceVariant   = Color(0xFFFBF8F0),
    onSurface        = Color(0xFF0B1020),
    onSurfaceVariant = Color(0xFF5B6472),
    outline          = Color(0xFFE2DFD5),
    outlineVariant   = Color(0xFFD6D2C5),
    error            = Color(0xFFB91C1C),
)
```

### 2.3 Sabit token'lar (tema-üstü)

```kotlin
object Telsiz {
    // Durum renkleri
    val Success = Color(0xFF15803D)
    val Danger  = Color(0xFFB91C1C)
    val Warn    = Color(0xFFA16207)
    val Streak  = Color(0xFFDC6803)

    // Bölüm renkleri (curriculum'dan, sabit)
    val B1Nato       = Color(0xFF1E3A8A)
    val B2Q          = Color(0xFF0F766E)
    val B3Elektronik = Color(0xFFB45309)
    val B4Frekans    = Color(0xFF0369A1)
    val B5Prosedur   = Color(0xFF15803D)
    val B6Trac       = Color(0xFF7C2D12)

    // Spacing
    val GapXs = 6.dp
    val GapSm = 8.dp
    val GapMd = 10.dp
    val GapLg = 12.dp
    val PageH = 16.dp

    // Radius
    val RadXs = 6.dp
    val RadSm = 8.dp
    val RadMd = 10.dp
    val RadLg = 14.dp
}
```

### 2.4 Tipografi

İki font ailesi. Google Fonts'tan indirip `res/font/` altına koy:

- **Inter** — 400 / 500 / 600 / 700 / 800
- **IBM Plex Mono** — 400 / 500 / 600 / 700

```kotlin
val SansFamily = FontFamily(/* Inter weights */)
val MonoFamily = FontFamily(/* IBM Plex Mono weights */)

val TelsizType = Typography(
    displaySmall = TextStyle(SansFamily, 22.sp, FontWeight.ExtraBold, letterSpacing = (-0.4).sp), // Ekran başlığı
    titleLarge   = TextStyle(SansFamily, 18.sp, FontWeight.Bold),                                  // Bölüm adı
    titleMedium  = TextStyle(SansFamily, 15.sp, FontWeight.SemiBold),                              // Liste başlığı
    labelLarge   = TextStyle(SansFamily, 14.sp, FontWeight.Bold),                                  // Buton
    bodyMedium   = TextStyle(SansFamily, 13.5.sp, FontWeight.Normal),                              // Gövde
    bodySmall    = TextStyle(SansFamily, 11.5.sp, FontWeight.Normal),                              // İkincil
    labelSmall   = TextStyle(MonoFamily, 10.5.sp, FontWeight.Bold, letterSpacing = 1.6.sp),        // Eyebrow MONO
)
```

**Kural:** Aşağıdakiler **her zaman** monospace (`MonoFamily`):
- Çağrı işaretleri (`TA2/CALL`), seviye (`Lv.4`), XP (`1240`)
- NATO harfleri, Q-kodları (`QRZ`), mors kodu
- Frekans değerleri (`14.205`), formüller (`V=IR`)
- Eyebrow etiketleri (`BÖLÜM 03/06 · ELEKT`)
- T-eksi gün sayacı, yüzdeler

---

## 3 · Bilgi mimarisi

```
MainActivity
└── NavHost(startDestination = "home")
    ├── home              → HomeScreen
    ├── bolum/{bolumId}   → BolumDetayScreen          (opsiyonel — Home'la aynı içerik)
    ├── lesson/{dersId}   → DersScreen                (tam ekran, bottom nav GİZLİ)
    ├── sinav             → SinavScreen
    ├── kutuphane         → KutuphaneScreen
    └── profil            → ProfilScreen

Bottom Nav (her ekranda görünür, `lesson` hariç):
[ANA SAYFA]  [DERSLER]  [SINAV]  [PROFİL]
"DERSLER" → kutuphane route'una gider.
```

---

## 4 · Veri modeli

```kotlin
// domain/model/Curriculum.kt
data class Bolum(
    val id: String,        // "b1".."b6"
    val no: Int,
    val kod: String,       // "NATO", "Q-KODE", ...
    val baslik: String,
    val aciklama: String,
    val glyph: String,     // "A·B·C", "QRZ", "V=IR", ...
    val renk: Long,        // 0xFF1E3A8A
    val dersler: List<Ders>,
)

data class Ders(
    val id: String,        // "b1l1".."b6q"
    val baslik: String,
    val alt: String,       // alt metin (önizleme)
    val sure: Int,         // dakika
    val sinav: Boolean = false,
)

enum class DersDurum { LOCKED, CURRENT, DONE }

data class Rozet(
    val id: String,
    val isim: String,
    val glyph: String,
    val aciklama: String,
    val kazanildi: Boolean,
)
```

**Önemli:** `data.js` içindeki müfredatı **birebir** Kotlin'e taşı. Asset olarak `assets/curriculum.json` yapıp parse etmek tercih edilir; metinleri kaybetme.

NATO, Q-kodu, mors, günün bilgisi havuzu da `data.js`'den birebir alınır.

---

## 5 · State yönetimi

```kotlin
data class UiState(
    val ilerleme: Map<String, DersDurum>,  // "b1l1" → DONE
    val xp: Int,
    val streak: Int,
    val tamamlananDers: Int,
    val examDays: Int,
    val examReady: Float,        // 0f..1f
    val denemeSinav: Int,
    val ortalamaPuan: Int,       // 0..100
    val zayifKonu: Int,
    val cagri: String = "TA2/YOUR",
)
```

İlk açılışta seed olarak HTML'deki **"Orta"** senaryosunu kullan:
`xp=1240, streak=12, tamamlananDers=12, examDays=38, examReady=0.42f, denemeSinav=3, ortalamaPuan=68, zayifKonu=5`.

İlerleme mantığı (`makeInitialState` Orta seviyesi, HTML'den):
- `b1` (NATO): tüm dersler `DONE`
- `b2` (Q): tüm dersler `DONE`
- `b3` (Elektronik): `b3l1`=DONE, `b3l2`=DONE, `b3l3`=CURRENT, geri kalan LOCKED
- `b4`, `b5`, `b6`: hepsi LOCKED

**Kalıcılık:** DataStore (Preferences) ile sakla. Anahtarlar: `xp`, `streak`, `cagri`, `lessons` (JSON map).

**Reducer aksiyonları:**
- `LessonCompleted(dersId)` → o dersi `DONE`, sonraki dersi `CURRENT` yap, `+15 XP` ekle, `tamamlananDers++`.
- `Reset` → seed state.

---

## 6 · Ekran-ekran build sırası

Bu sırayla çalış. Her ekran kabul kriterini geçmeden sonrakine geçme.

### 6.1 `HomeScreen` (önce bu — referans ekran)

**Yapı (yukarıdan aşağı):**

1. **TopBar** — logo glifi (kare içinde `TO`), `TELSIZ.OKULU` başlık (mono), altında `{cagri} · Lv.{seviye}`. Sağda streak chip (🔥 + sayı) ve XP chip (⭐ + sayı), her ikisi de mono.
2. **CompactStats** — 3 mini kart yatay: `[Lv {n} · {xp} XP + bar]`, `[T-{days} + hazırlık bar]`, `[×{zayifKonu} zayıf konu]`.
3. **ChapterSelector** — 3px sol renk şeritli kart. `[←]` `EYEBROW: BÖLÜM 0X/06 · KOD` `[→]` üst satırda, altında bölüm başlığı, `dersler tamamlanan/toplam` progress bar, ve `● ● ○ ○ ○ ○` dot indicator.
   - `←/→` ile aktif bölüm değişir.
   - Kilitli bölüme geçince Snackbar: `"Önce 'XYZ' bölümünü tamamla"`.
4. **Continue button** — siyah/surface bg, `▶ DEVAM ET · {sure} DK` eyebrow + ders başlığı + `→` ok. Tıklayınca `lesson/{dersId}`.
5. **ChapterShowcase** — `when (aktifBolum.id)` ile 6 farklı composable'dan birini render eder (bkz. 6.6).
6. **Ders Listesi** — `"Dersler · {x}/{y}  ▓░ {%}"` başlığı, sonra her ders için `LessonRow`:
   - Sol: durum ikonu (✓ done / ▶ current / 🔒 locked / Q sinav).
   - Orta: ders başlığı + alt metin.
   - Sağ: `{sure} dk` mono.
   - Durumlara göre opacity ve enabled değişir.
7. **DailyMorse** — `"GÜNÜN BİLGİSİ · {kod}"` eyebrow, ortada büyük mors kodu (mono, letterSpacing), altında açıklama. `GUNUN_BILGISI`'dan ilk eleman.
8. **GrillMeFAB** — sağ altta `⚡ DRILL` floating button. Sürekli pulse animasyonu (scale + opacity). Tıklayınca `ModalBottomSheet` açar.
9. **BottomNav** — `Material3 NavigationBar`, 4 sekme.

**Kabul:** Renkler Spektrum şemasında doğru, her tıklama doğru route'a gider, bölüm seçici döngüsel çalışır, kilitli bölüm seçilince Snackbar çıkar.

### 6.2 `DersScreen` (Lesson + Quiz)

Tam ekran. Bottom nav YOK. Üstte `[×]` kapat + lineer progress bar `{q.i+1}/{q.total}`.

Soru akışı:
- 4 soru. Her soru: eyebrow (`NATO ALFABESİ · ÇOKTAN SEÇMELİ`), prompt, büyük mono subtitle (örn. harf), 4 seçenek.
- Seçilince accent border. Bottom CTA `Kontrol et` aktif olur.
- `Kontrol et` → renkli feedback (yeşil/kırmızı). CTA `Devam et` olur.
- 4 soru sonu → bitiş ekranı: `Tebrikler! · {dogru}/4 doğru · +{xp} XP`.
- `Devam et` → `LessonCompleted` dispatch + Home'a pop.

Soru havuzları:
- `b1`: `NATO[]`'dan harf seç, fonetik sor + 3 yanlış (diğer NATO harflerinden).
- `b2`: `QKODES[]`'tan kod → anlam + 3 yanlış.
- `b3..b6`: `screens.jsx`'teki `map.b3 / b4 / b5 / b6` havuzlarını birebir Kotlin'e kopyala.

### 6.3 `SinavScreen`

- Üstte koyu/surface hero: `T-MINUS {examDays} gün` + 3 mini stat (`HAZIR %`, `DENEME #`, `ORT %`).
- Altında 5 satır (stub butonlar, henüz Lesson'a giderler veya Toast):
  - `Tam Deneme Sınavı · 40 soru · 60 dk` → `[BAŞLA]`
  - `Bölüm Sınavı · Tek bölüm` → `[SEÇ]`
  - `Hızlı 10 · ~5 dk` → `[BAŞLA]`
  - `Çıkmış Sorular Arşivi` → `[AÇ]`
  - `Zayıf Konu Sınavı · 5 konu × 10 soru` → `[ÇALIŞ]`

### 6.4 `KutuphaneScreen`

`TabRow` ile 4 sekme:
- **NATO Alfabesi** — `LazyVerticalGrid(2 sütun)`, her satır: büyük harf (mono) | kelime (sans) + telaffuz (mono small).
- **Q Kodları** — Liste, `QKODES` üzerinden: sol mono `QRZ`, sağ anlam.
- **Morse** — `LazyVerticalGrid(4 sütun)`, A-Z için mors kodu (sabit map, `screens.jsx`'ten al).
- **Prosedür** — OVER/OUT/ROGER/WILCO/COPY/BREAK/CQ/MAYDAY/PAN-PAN açıklamaları. MAYDAY ve PAN-PAN kart border'ı `Danger` renkli.

### 6.5 `ProfilScreen`

1. **Hero card** — Sol kare içinde mono `TA2` callsign rozeti (accent bg). Sağda: `ÇAĞRI · {cagri} · A SINIFI ADAYI`, `Seviye {n} · Operatör`, `{xp}/{nextXp} XP` lineer bar.
2. **3 stat chip** — `[{streak} STREAK]`, `[{tamamlananDers} DERS]`, `[{kazanilan}/{toplam} ROZET]`.
3. **Rozetler grid** — `LazyVerticalGrid(3 sütun)`, `ROZETLER` üzerinden. Kazanılan: aksent border + bg, glyph parlak. Kazanılmamış: opacity 0.4 + 🔒 overlay.
4. **Bölüm İlerlemesi** — Her bölüm için tek satır: kod chip (bölüm renginde) + başlık + sağda progress bar + `%`.

### 6.6 ChapterShowcase composable'ları

Her bölüm için ayrı dosya: `ui/showcase/ShowcaseB1Nato.kt`, `ShowcaseB2Q.kt`, vb. Yapı:
- Ortak shell: kart, eyebrow, başlık, ana içerik alanı.
- **B1 NATO**: 9 sütun harf grid (Canvas DrawText veya küçük composable). Tıklanan harfin fonetik + telaffuzu üstte büyük mono.
- **B2 Q-Kodu**: Mono transcript (`TA1ABC: CQ CQ DE TA1ABC...`). Altta 6 küçük kart (QRZ, QSL, QSO, QTH, QRM, QRP) — her kart: kod (büyük mono) + anlam (küçük sans).
- **B3 Elektronik**: SVG yerine `Canvas { drawPath(...) }` ile ikizkenar üçgen, köşelere `V`, `I`, `R` (mono). Altta 4 sembol kartı: Volt/Amper/Ohm/Watt — birim, sembol, açıklama.
- **B4 Frekans**: `Canvas` ile 7 bar bar-chart. Bantlar: 80m, 40m, 20m, 15m, 10m, 2m, 70cm. Tıklanan bantın MHz değeri büyük mono'da gösterilir.
- **B5 Prosedür**: Mono CQ transcript bloğu. Altta 5 chip: OVER, ROGER, WILCO, MAYDAY (kırmızı border), PAN-PAN (kırmızı border).
- **B6 TRAC**: Büyük mono yüzde (`%42`), T-eksi rozet, 3 mini stat (`deneme/ortalama/zayıf`), altta "İpucu" kartı.

### 6.7 Grill Me modalı (FAB → BottomSheet)

`ModalBottomSheet`:
- Eyebrow: `⚡ HIZLI DRILL · {aktifBolumKodu}`.
- Bir rastgele soru (aktif bölümün havuzundan).
- 4 seçenek, `Kontrol et`, ✓/✕ feedback, `Yeni Soru` / `Kapat`.
- XP **eklemez** — sadece pratik.

---

## 7 · Etkileşim detayları

- **Bölüm değişimi**: `←/→` veya dot tıklama → `aktifBolum` state değişir → showcase ve ders listesi yeniden render. Kart için 250ms `AnimatedContent` slide+fade.
- **Lesson tamamlandı animasyonu**: ✓ ikonu yeşil pulse (scale 1→1.2→1).
- **FAB pulse**: 2sn döngüsünde halka opacity 0.7→0 + scale 1→1.8.
- **Locked tap**: Snackbar 2.8sn süreyle: `"Önce 'X' bölümünü tamamla"`.
- **Geri tuşu**: Lesson içinde önce confirm dialog (`Çıkmak istiyor musun? İlerleme kaybolur`).

---

## 8 · Asset listesi (üreteceklerin)

```
app/src/main/
├── java/com/telsiz/okulu/
│   ├── MainActivity.kt
│   ├── ui/
│   │   ├── theme/   {Color.kt, Type.kt, Theme.kt, Tokens.kt}
│   │   ├── components/  {LessonRow.kt, ChapterSelector.kt, ProgressBar.kt, MonoText.kt, GrillMeFab.kt, StatChip.kt, DotIndicator.kt}
│   │   ├── home/    HomeScreen.kt
│   │   ├── lesson/  {DersScreen.kt, QuestionState.kt, QuestionPools.kt}
│   │   ├── sinav/   SinavScreen.kt
│   │   ├── library/ KutuphaneScreen.kt
│   │   ├── profil/  ProfilScreen.kt
│   │   ├── showcase/ {ShowcaseB1Nato.kt ... ShowcaseB6Trac.kt}
│   │   └── nav/     {AppNav.kt, BottomBar.kt}
│   ├── data/
│   │   ├── Curriculum.kt    (data.js → Kotlin data)
│   │   ├── NatoTable.kt
│   │   ├── QCodesTable.kt
│   │   ├── MorseTable.kt
│   │   └── Rozetler.kt
│   └── domain/
│       ├── UiState.kt
│       └── AppViewModel.kt
└── res/
    ├── font/        {inter_*.ttf, ibm_plex_mono_*.ttf}
    ├── values/      {strings.xml, themes.xml}
    └── drawable/    (logo glifi vb. — yoksa Compose Canvas ile çiz)
```

---

## 9 · Yapma / Yapmama listesi

**Yap:**
- Tüm string'leri `strings.xml`'de Türkçe olarak topla.
- Renk/spacing/font için **sadece** tema/tokens dosyalarındaki değerleri kullan. Hard-code etme.
- Mono fontu mono olması gereken her yerde kullan (bkz. §2.4).
- Bölüm renklerini `Bolum.renk`'ten oku, hard-code etme.
- Lesson içinde sistem geri tuşunu yakala.
- `LazyColumn` kullan, `Column { Modifier.verticalScroll }` ile uzun listeleri render etme.

**Yapma:**
- Tweaks panelini port etme. Settings ekranı **yok**, en azından MVP'de.
- Material 3 mavi varsayılan rengini bırakma — tüm composable'lar `MaterialTheme.colorScheme` üzerinden gitmeli.
- Emoji ikon kullanma — sistem ikonları için `Icons.*` (Material), bölüm/durum glifleri için **monospace metin**.
- SVG/vektör drawable üretme — gerçek tasarımcı geldiğinde değişecek. Yer tutucular için `Canvas` ile çiz veya monospace metin.
- "Duolingo" markasını ya da görsellerini hiçbir yere koyma. Sadece *mekanik* esinlenme.
- `Toast` kullanma — `Snackbar` kullan.

---

## 10 · Kabul kriterleri (DoD)

1. Uygulama açılır, Home Spektrum temasında render olur, console error'sız.
2. Bölüm seçici `←/→` ile gezilebilir, kilitli bölüm seçilirken Snackbar çıkar.
3. "Devam Et" → `b3l3` (Güç ve Devre) Lesson ekranını açar, 4 soru çözülünce Home'a +15 XP ekleyerek döner.
4. Bottom nav 4 sekme arasında geçiş çalışır; Lesson içinde bottom nav görünmez.
5. Grill Me FAB her ana ekranda görünür, bottom sheet açılır, soru gösterir.
6. Profil ekranı 9 rozet grid'ini ve 6 bölüm ilerleme barını gösterir.
7. Kütüphane 4 sekmesi de render olur (NATO 26 harf, Q 10 kod, mors 26 harf, prosedür 9 terim).
8. Uygulama kapanıp açıldığında XP ve ilerleme korunmuştur (DataStore).
9. Renkler/font sample HTML prototipiyle yan yana koyulduğunda token-perfect (görsel tek tek pixel değil, ama hex'ler ve spacing aynı).

---

## 11 · Çalışma sırası — önerilen commit ritmi

1. `feat(theme): scaffold colors, type, tokens`
2. `feat(data): port curriculum + nato + qcodes + morse + rozetler from data.js`
3. `feat(nav): MainActivity + NavHost + bottom nav scaffold`
4. `feat(home): top bar + compact stats + chapter selector`
5. `feat(home): continue button + lessons list + daily morse`
6. `feat(home): grill-me FAB + bottom sheet`
7. `feat(showcase): B1 NATO interactive grid`
8. `feat(showcase): B2..B6 (her birini ayrı commit)`
9. `feat(lesson): question state machine + 4-question flow + completion`
10. `feat(sinav): exam menu screen`
11. `feat(library): tabs + 4 tables`
12. `feat(profil): hero + badges + chapter progress`
13. `feat(persistence): DataStore wiring`
14. `chore(a11y): contentDescription pass`
15. `chore(polish): animasyonlar + Snackbar mesajları`

---

## 12 · Çıktı

Tamamlandığında `README.md` (Android repo'nun kendi README'si) yaz:
- Kurulum (Android Studio Iguana+, JDK 17, Gradle 8+).
- Mimari özet (Compose + ViewModel + DataStore).
- Bu HTML prototipine referans link.
- Bilinen eksikler ve sonraki adımlar.

---

**Kaynak gerçeği:** `Telsiz Okulu.html` ve içindeki `.jsx`/`data.js` dosyaları.
**Soru çıkarsa:** önce kaynak dosyaya bak, sonra `README.md`'ye, sonra sor.
**Tek hedef:** HTML prototipinin yaşayan, üretim-kalitesinde Android versiyonu.
