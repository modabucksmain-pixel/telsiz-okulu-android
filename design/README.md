# Telsiz Okulu — Ana Menü Prototipi

> **Amatör telsizcilik öğreten Duolingo-tarzı Android uygulamasının** ana menü, ders akışı, sınav, kütüphane ve profil ekranlarını içeren tam interaktif HTML prototipidir.
> 
> Bu prototip **tasarım referansıdır** — üretim Android koduna port edilmek üzere hazırlanmıştır.

```
┌─────────────────────────────────────────────────┐
│  TELSIZ.OKULU         TA2/CALL · Lv.4    🔥12 ⭐1240 │
│  ┌───────────────────────────────────────────┐  │
│  │ ▸ BÖLÜM 03/06 · ELEKT                     │  │
│  │   ←  Elektronik Temeller  →               │  │
│  │   ▓▓▓▓▓░░░░░░░░  2/5                      │  │
│  │   ● ● ● ○ ○ ○                              │  │
│  └───────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────┐  │
│  │ ▶ DEVAM ET · 8 DK                         │  │
│  │   Güç ve Devre                       →    │  │
│  └───────────────────────────────────────────┘  │
│      [ Ohm üçgeni: V/I/R + sembol kartları ]   │
│                                                 │
│      Dersler · 2/5  ▓▓▓░░░ 40%                  │
│      ✓ Temel Kavramlar          7 dk            │
│      ✓ Ohm Kanunu               8 dk            │
│      ▶ Güç ve Devre             8 dk            │
│      🔒 Yarı İletkenler          7 dk            │
│      🔒 Bölüm Sınavı [Q]        10 dk            │
│                                          ⚡     │
│                                        DRILL    │
│  [🏠 ANA  📚 DERSLER  ✏ SINAV  👤 PROFİL]      │
└─────────────────────────────────────────────────┘
```

---

## İçindekiler

- [Hızlı Başlangıç](#hızlı-başlangıç)
- [Konsept](#konsept)
- [Teknoloji](#teknoloji)
- [Dosya Yapısı](#dosya-yapısı)
- [Tasarım Sistemi](#tasarım-sistemi)
- [Ekranlar](#ekranlar)
- [Bölüm Showcase'leri](#bölüm-showcaseleri)
- [Etkileşim Akışları](#etkileşim-akışları)
- [Durum Yönetimi](#durum-yönetimi)
- [Tweaks Paneli](#tweaks-paneli)
- [Müfredat Verisi](#müfredat-verisi)
- [Android Port Rehberi](#android-port-rehberi)
- [Genişletme Rehberi](#genişletme-rehberi)
- [Bilinen Sınırlamalar](#bilinen-sınırlamalar)

---

## Hızlı Başlangıç

```bash
# Sadece tarayıcıda aç — build adımı yok
open "Telsiz Okulu.html"
```

Tüm dosyalar statik. Herhangi bir HTTP sunucu yeterli:

```bash
python3 -m http.server 8000
# → http://localhost:8000/Telsiz Okulu.html
```

**Sağ üstteki Tweaks** butonundan tema, aksent, ilerleme seviyesi ve hızlı ekran navigasyonu kullanılabilir.

---

## Konsept

**Telsiz Okulu**, amatör telsizcilik (Türkiye'de TRAC sınavına hazırlık) öğretmek için tasarlanmış oyunlaştırılmış bir öğrenme uygulamasıdır.

### Hedef Kullanıcı
TRAC (Türkiye Radyo Amatörleri Cemiyeti) A/B sınıfı lisans sınavına hazırlanan, sıfırdan başlayan amatör telsizci adayları.

### Pedagojik Yaklaşım
| Özellik | Açıklama |
|---|---|
| **6 ardışık bölüm** | NATO Alfabesi → Q Kodları → Elektronik → Frekans → Prosedür → TRAC |
| **Bölüm-içi 5 ders** | 4 öğretme dersi + 1 bölüm sınavı |
| **Sıralı kilit sistemi** | Önceki bölümü tamamlamadan sonrakini açamazsın |
| **XP / Seviye** | Acemi (Lv1) → TRAC Adayı (Lv7) arası 7 seviye |
| **Streak** | Günlük seri takibi (alev simgesi) |
| **Spaced repetition** | "Zayıf konularını tekrar et" — yanlış cevaplananlar |
| **Grill Me ⚡** | Her ekrandan erişilebilen anlık rastgele soru sorma |
| **TRAC sayacı** | T-eksi gün + hazırlık yüzdesi |

### Konu Kapsamı
1. **NATO Alfabesi** — 26 harf + 10 rakam, fonetik telaffuz
2. **Q Kodları** — QRZ, QSL, QSO, QTH dahil 15+ uluslararası Q kodu
3. **Elektronik Temeller** — Volt/Amper/Ohm/Watt, Ohm Kanunu, devre elemanları
4. **Frekans ve Bantlar** — HF/VHF/UHF, dalga boyu, Türkiye bandplanı
5. **Telsiz Prosedürleri** — CQ/QSO yapısı, çağrı işaretleri, acil durum
6. **TRAC Sınav Hazırlık** — Mevzuat, lisans sınıfları, çıkmış sorular

---

## Teknoloji

| Katman | Teknoloji | Not |
|---|---|---|
| **Render** | React 18.3.1 (UMD) | Babel ile inline JSX transpile |
| **Görsel** | Vanilla CSS + inline styles | CSS framework yok |
| **Font** | Inter + IBM Plex Mono (Google Fonts) | İkili tipografi |
| **Animasyon** | CSS keyframes + transitions | JS animation kütüphanesi yok |
| **State** | useState + useReducer | Redux/Zustand yok |
| **Routing** | Manuel switch (URL'siz) | React Router yok |
| **Persistence** | postMessage → host (Tweaks ayarları) | LocalStorage yok |
| **Build** | Yok — direkt çalışır | Webpack/Vite yok |

### Neden Bu Stack?
- **Hızlı iterasyon**: build adımı olmadığından her değişiklik anında görülür
- **Taşınabilir**: tek HTML dosyası + CDN script'leri
- **Android port'a hazır**: state ve component yapısı Compose'a 1:1 çevrilebilir

---

## Dosya Yapısı

```
.
├── Telsiz Okulu.html         ← Giriş noktası (script tag'ler burada)
├── data.js                   ← Müfredat + NATO + Q kodları + rozetler
├── theme.js                  ← Tema token üreteci (3 varyant × 3 aksent)
├── tweaks-panel.jsx          ← Tweaks paneli (host protokolü)
├── home.jsx                  ← Ana sayfa + bölüm seçici + Grill Me modalı
├── chapter-pages.jsx         ← Her bölümün konuya özel showcase widget'ı
├── screens.jsx               ← Bölüm detay, ders, sınav, kütüphane, profil
├── app.jsx                   ← App kabuğu, router, bottom nav, durum
└── design_handoff_ana_menu/  ← Claude Code'a verilecek handoff paketi
    ├── README.md
    └── prototype/            ← Bu klasörün ZIP kopyası
```

### Yükleme Sırası (`Telsiz Okulu.html` içinde)
1. React + ReactDOM + Babel (CDN)
2. `data.js` — global veriyi `window.CURRICULUM`, `window.NATO` vb. olarak yerleştirir
3. `theme.js` — `window.makeTheme(variant, accent)` fonksiyonunu tanımlar
4. `tweaks-panel.jsx` — `window.TweaksPanel`, `window.useTweaks` vb.
5. `home.jsx` — `Home`, `Mono`, `Card`, `Bar`, `chapterProgress`, `SpectrumBg`
6. `chapter-pages.jsx` — `window.ChapterShowcase`
7. `screens.jsx` — `BolumDetay`, `Ders`, `Sinav`, `Kutuphane`, `Profil`, `Pratik`
8. `app.jsx` — `App` ve ReactDOM mount

> **Önemli:** Her Babel `<script>` ayrı scope'a sahip. Component'leri paylaşmak için `Object.assign(window, {...})` veya `window.X = X` kullanılıyor.

---

## Tasarım Sistemi

### Tema Varyantları (3)

| Varyant | Görünüm | Kullanım Senaryosu |
|---|---|---|
| **Spektrum** ⭐ | Koyu mod, telsiz panel hissi | Varsayılan — gece çalışma, atmosferik |
| **Sade** | Aydınlık krem, mühendislik | Profesyonel, gündüz dostu |
| **Saha** | Daha sıcak krem, blueprint kağıdı | Saha not defteri estetiği |

Tema değiştirmek için: **Tweaks → Varyasyon → Tema**

### Aksent Renkleri (3)

| Aksent | Hex (Light) | Hex (Dark) | Karakter |
|---|---|---|---|
| **Mavi** ⭐ | `#1E3A8A` | `#5BD9C8` (teal-cyan) | Klasik, kurumsal |
| **Yeşil** | `#0F766E` | — | Doğal, dingin |
| **Amber** | `#92400E` | — | Sıcak, vintage radyo |

### Bölüm Renkleri (Sabit)
Her bölümün kendine has bir rengi var; tema veya aksentten bağımsız:

| Bölüm | Renk | Hex |
|---|---|---|
| 01 NATO | Lacivert | `#1E3A8A` |
| 02 Q Kodları | Teal | `#0F766E` |
| 03 Elektronik | Amber-kahve | `#B45309` |
| 04 Frekans | Cyan-mavi | `#0369A1` |
| 05 Prosedür | Yeşil | `#15803D` |
| 06 TRAC | Koyu kahve | `#7C2D12` |

### Durum Renkleri
```
SUCCESS  #15803D    Yeşil      → doğru cevap, tamamlandı
DANGER   #B91C1C    Kırmızı    → yanlış cevap, MAYDAY
WARN     #A16207    Sarı       → orta seviye uyarı
STREAK   #DC6803    Turuncu    → streak alevi, Grill Me FAB
```

### Tipografi

| Aile | Kullanım |
|---|---|
| **Inter** (sans) | Gövde metni, ekran başlıkları, ders/bölüm isimleri |
| **IBM Plex Mono** | Teknik etiketler, sayılar (XP, frekans, T-eksi), Q kodları, NATO harfleri, mors |

**Tipografik ölçek:**
```
22sp ExtraBold (-0.4 spacing)   → Ekran başlığı
18sp Bold                       → Bölüm adı (seçici kartında)
15sp SemiBold                   → Liste öğesi başlığı
14sp Bold                       → Buton metni
13.5sp Regular                  → Gövde metni
11.5sp Regular                  → İkincil metin
10.5sp Bold (1.6 spacing) MONO  → Eyebrow / kategori etiketi
10sp Bold (1.4 spacing) MONO    → "Mono" küçük etiketi
```

### Boşluk ve Yarıçap
```
Radius LG   14px   → Kartlar, hero kartı
Radius MD   10px   → Liste satırları, butonlar
Radius SM    8px   → Chip'ler, küçük ikonlar
Radius XS    6px   → En küçük öğeler

Padding     16px (yatay genel)
Gap          8px / 10px / 12px (esnek)
```

### İkonografi Felsefesi
HTML prototipinde **emoji ve SVG yerine monospace glif** kullanılıyor:
- NATO bölümü: `"A·B·C"` etiketi
- Q kodları: `"QRZ"`
- Elektronik: `"V=IR"`
- Frekans: `"14.205"`
- TRAC: `"TA1"`

Sadece sistem ikonları SVG ile çizildi (geri butonu, alt nav, oklar).

---

## Ekranlar

Toplam **7 ekran** — `App` içinde `nav.screen` ile yönlendiriliyor:

```
home       → Ana sayfa (varsayılan giriş)
bolum      → Bölüm detay sayfası
lesson     → Ders / egzersiz akışı
sinav      → TRAC sınav menüsü
kutuphane  → NATO/Q/Morse/Prosedür referansı
profil     → Operatör profili + rozetler
pratik    → Zayıf konu pratiği (landing)
```

### 1. Ana Sayfa (`Home` — `home.jsx`)

**Hiyerarşi (yukarıdan aşağı):**

```
┌─ TopBar ─────────────────────────────────────────┐
│  [Logo] TELSIZ.OKULU                             │
│        TA2/CALL · Lv.4         🔥12 ⭐1240        │
├─ CompactStats ───────────────────────────────────┤
│  [4 ████░░ 1240 XP]  [T-38 ▓░░]  [×5]            │
├─ ChapterSelector ────────────────────────────────┤
│  ┌── 3px renk-şeritli sol kenar ──────────┐     │
│  │ [←]  ▸ BÖLÜM 03/06 · ELEKT       [→]    │     │
│  │      Elektronik Temeller                │     │
│  │      ▓▓▓▓▓░░░░░░ 2/5                    │     │
│  │      ● ● ● ○ ○ ○                         │     │
│  └────────────────────────────────────────┘     │
├─ Continue button (siyah bg) ─────────────────────┤
│  ▶ DEVAM ET · 8 DK                              │
│    Güç ve Devre                            →    │
├─ ChapterShowcase (seçili bölüme özgü) ───────────┤
│  [Ohm üçgeni V/I/R + Volt/Amper/Ohm/Watt]       │
├─ Dersler ────────────────────────────────────────┤
│  Dersler · 2/5     ▓▓▓░░ 40%                    │
│  [✓] Temel Kavramlar              7 dk           │
│  [✓] Ohm Kanunu                   8 dk           │
│  [▶] Güç ve Devre                 8 dk           │
│  [🔒] Yarı İletkenler              7 dk           │
│  [Q] Bölüm Sınavı [SINAV chip]   10 dk           │
├─ DailyMorse ─────────────────────────────────────┤
│  GÜNÜN BİLGİSİ              QSY                  │
│  [─·─·─  · ─·─·─  ─·──  ··]                     │
│  QSY = "Frekans değiştir"...                     │
├─ Floating Button ────────────────────────────────┤
│                                       [⚡ DRILL] │
└─ BottomNav ──────────────────────────────────────┘
   [🏠 ANA SAYFA] [📚 DERSLER] [✏ SINAV] [👤 PROFİL]
```

**Tasarım kararları:**
- **Tek bölüm görünür**: Önceki tasarımda 6 bölümün hepsinin zigzag yolu vardı; çok bilgi yoğundu. Şimdi seçici ile bir bölüm odaklanıyor.
- **Inline showcase + dersler**: Bölüm detay sayfasına gitmeye gerek kalmadan tüm bilgi ana sayfada.
- **Continue butonu**: Yapay bir hero card yerine, sıradaki dersi tek satırda çağrı yapan koyu CTA.

### 2. Bölüm Detay (`BolumDetay` — `screens.jsx`)

Ana sayfayla **neredeyse aynı içerik**: Showcase + ders listesi. Aradaki tek fark üst kısımda geri butonu var ve bottom nav'da "DERSLER" sekmesi aktif görünüyor.

> **Not:** Bu ekran, kullanıcı bottom nav'dan "DERSLER" sekmesine bastığında veya ana sayfadan bölüm seçici dışında bir yolla erişildiğinde devreye girer. Mevcut akışta Ana Sayfa zaten aynı içeriği sergilediği için bu ekran biraz yedek.

### 3. Ders / Egzersiz (`Ders` — `screens.jsx`)

Tam ekran çoktan seçmeli soru akışı.

**Yapı:**
```
[×]  ▓▓▓░░░░░░░░ 2/4

NATO ALFABESİ · ÇOKTAN SEÇMELİ

"M" harfinin NATO karşılığı nedir?

         ┌────┐
         │  M │  ← Big monospace subtitle
         └────┘

[A] Mike    ← Seçili: accent border
[B] Echo
[C] November
[D] Kilo

──────────────────────────────────
✓ Doğru
[ Kontrol et → Devam et ]
```

**Akış:**
1. Soru görünür → kullanıcı bir şık seçer
2. "Kontrol et" basılınca renkli feedback (yeşil/kırmızı)
3. "Devam et" sonraki soruya
4. 4 soru sonunda → **bitiş ekranı** (doğru/başarı/+XP)
5. "Devam et" → bolum detay sayfasına döner, XP eklenir, ders "done" işaretlenir

**Soru üretimi:**
- NATO bölümü: `NATO[]` listesinden rastgele harf → fonetik + 3 yanlış
- Q kodları: `QKODES[]` listesinden rastgele kod → anlam + 3 yanlış
- Diğer 4 bölüm: `screens.jsx` içinde elle yazılmış soru havuzları (`map.b3`, `b4`, `b5`, `b6`)

### 4. Sınav Menüsü (`Sinav` — `screens.jsx`)

```
┌────────────────────────────────────────┐
│  T-MINUS                               │
│  38 gün                                │
│  Bir sonraki TRAC sınavına kadar       │
│  [HAZIR 42%] [DENEME 3] [ORT 68%]      │
└────────────────────────────────────────┘
• Tam Deneme Sınavı  · 40 soru · 60 dk     [BAŞLA]
• Bölüm Sınavı       · Tek bölüm           [SEÇ]
• Hızlı 10           · ~5 dk               [BAŞLA]
• Çıkmış Sorular Arşivi                    [AÇ]
• Zayıf Konu Sınavı  · 5 konu × 10 soru    [ÇALIŞ]
```

### 5. Kütüphane (`Kutuphane` — `screens.jsx`)

4 sekmeli referans:
- **NATO Alfabesi** — 26 harf, 2 sütunlu grid, telaffuz dahil
- **Q Kodları** — 10 kod + anlam, liste
- **Morse** — 26 harf, 4 sütunlu grid, monospace mors kodu
- **Prosedür** — OVER/OUT/ROGER/WILCO/COPY/BREAK/CQ/MAYDAY/PAN-PAN açıklamaları

### 6. Profil (`Profil` — `screens.jsx`)

```
┌──────────────────────────────────────────┐
│  [TA2] ÇAĞRI · TA2/CALL · A SINIFI ADAYI │
│        Seviye 4 · Operatör               │
│        ▓▓▓▓░░░░░ 1240/2000 XP            │
├──────────────────────────────────────────┤
│  [12 STREAK] [12 DERS] [3/9 ROZET]       │
├─ Rozetler ───────────────────────────────┤
│  3×N grid: 9 rozet                       │
│  - İlk Adım (01)        ✓ kazanıldı      │
│  - NATO Çaylağı (A·G)   ✓                │
│  - 7 Gün Seri (07)      ✓                │
│  - NATO Ustası (A·Z)    🔒              │
│  - ...                                   │
├─ Bölüm İlerlemesi ───────────────────────┤
│  B01 NATO Alfabesi      ▓▓▓▓▓ 100%       │
│  B02 Q Kodları           ▓▓▓▓▓ 100%       │
│  B03 Elektronik Temeller ▓▓░░░  40%       │
│  ...                                     │
└──────────────────────────────────────────┘
```

### 7. Pratik (`Pratik` — `screens.jsx`)

Zayıf konular pratiği için landing ekranı. Ana sayfadaki `×5` chip'ine tıklandığında açılır.

---

## Bölüm Showcase'leri

Her bölümün **konuya özel kendi widget'ı** var (`chapter-pages.jsx`). Ana sayfada ve bölüm detay sayfasında üst kısımda görünür.

### B1 — NATO Alfabesi: İnteraktif Alfabe Izgarası
- 9 sütunlu 26 harf grid'i
- Bir harfe dokun → üstte büyük harf + fonetik + telaffuz
- Hover/tap → seçili state değişir

### B2 — Q Kodları: Örnek QSO Transkripti
- TA1ABC ↔ DL2X bağlantı diyalogu
- 6 sık Q-kodu kartı (QRZ, QSL, QSO, QTH, QRM, QRP)

### B3 — Elektronik: Ohm Üçgeni
- SVG ile çizilmiş V/I/R üçgeni
- Formüller: `V = I·R`, `I = V/R`, `R = V/I`
- 4 sembol kartı: Volt, Amper, Ohm, Watt

### B4 — Frekans: Spektrum Grafiği
- 7 bant bar chart (Canvas / SVG)
- Bant tıklanınca büyük frekans değeri görünür
- Bantlar: 80m, 40m, 20m, 15m, 10m, 2m, 70cm

### B5 — Prosedürler: CQ Çağrısı Örneği
- Monospace transkript: `"CQ CQ CQ, this is TANGO ALPHA ONE..."`
- 5 anahtar terim listesi (OVER, ROGER, WILCO, MAYDAY, PAN-PAN — MAYDAY/PAN-PAN kırmızı çerçeveli)

### B6 — TRAC Sınav: T-Eksi Sayaç + Hazırlık
- Büyük monospace yüzde göstergesi
- T-eksi gün sayısı renkli rozet
- 3 mini stat: deneme/ortalama/zayıf
- "İpucu" kartı

---

## Etkileşim Akışları

### A) Ana Sayfa → Ders Çözme
```
Ana Sayfa
   ↓ (Bölüm seçici ← → ile bölüm seç)
Bölüm değişir, sayfa flash animasyonu yapar
   ↓ (Devam Et butonu veya ders satırı)
Ders ekranı (lesson)
   ↓ (4 soru çöz)
Bitiş ekranı (+XP)
   ↓ (Devam et)
Bolum Detay (XP eklenir, sonraki ders açılır)
```

### B) Grill Me ⚡ (Hızlı Drill)
```
Herhangi bir ekran (sağ alt FAB)
   ↓ (⚡ DRILL tıkla)
Bottom Sheet açılır
   ↓ (Aktif bölümden rastgele soru)
Şık seç → Kontrol et
   ↓ (✓ doğru veya ✕ yanlış geri bildirim)
Kapat (XP eklenmiyor, sadece pratik)
```

### C) Kilitli Bölüm Seçimi
```
Bölüm seçicide kilitli bölüme dokun
   ↓
Toast belirir: "Önce 'XYZ' bölümünü tamamla"
   ↓
Otomatik kapanır (2.8sn) → seçim değişmez
```

### D) Bottom Navigation
```
Ana Sayfa  ↔  Dersler  ↔  Sınav  ↔  Profil
   ↓
Lesson ekranında BottomNav GİZLİ (tam ekran odak)
```

### E) Tema Değiştirme (Tweaks)
```
Sağ üst Tweaks butonu
   ↓
Panel açılır → Tema/Aksent/İlerleme seç
   ↓
Tüm uygulama anında yeniden render
postMessage ile host'a kaydedilir (dosyaya yazılır)
```

---

## Durum Yönetimi

### State Tree (`app.jsx`)
```javascript
state = {
  ilerleme: {                           // her dersin durumu
    'b1l1': 'done',
    'b1l2': 'done',
    'b1q':  'current',
    'b2l1': 'locked',
    // ...
  },
  xp: 1240,                             // toplam XP
  streak: 12,                           // gün serisi
  tamamlananDers: 12,                   // tamamlanan ders sayısı
  examDays: 38,                         // T-eksi gün
  examReady: 0.42,                      // sınav hazırlık (0-1)
  denemeSinav: 3,                       // çözülen deneme sayısı
  ortalamaPuan: 68,                     // ortalama puan %
  zayifKonu: 5,                         // tekrar gerektiren konu sayısı
}
```

### Reducer Aksiyonları
| Action | Etki |
|---|---|
| `reset` | İlerleme seviyesi (Tweaks) değiştiğinde tüm state'i yeniden kurar |
| `complete-lesson` | Dersi `done` işaretler, sonrakini `current` yapar, +XP ekler |

### İlerleme Seviyeleri (Tweaks ile değişir)
`makeInitialState(progress)` 3 senaryo üretir:

| Seviye | XP | Streak | Tamamlanan | T-eksi | Hazır |
|---|---|---|---|---|---|
| **Yeni** | 35 | 1 | 0 | 120 gün | 5% |
| **Orta** ⭐ | 1240 | 12 | 12 | 38 gün | 42% |
| **Sınav Arifesi** | 3850 | 47 | 28 | 9 gün | 86% |

### Kalıcılık
- **State**: yok — sayfa yenilenince Tweaks'teki "İlerleme" seçimine göre sıfırlanır
- **Tweaks**: `postMessage` ile host'a (`__edit_mode_set_keys`) bildirilir, host JSON bloğunu dosyaya yazar (`EDITMODE-BEGIN/END` arası)

> **Üretim ortamında**: State, DataStore'a (Android) veya LocalStorage'a (web) kaydedilmeli.

---

## Tweaks Paneli

Sağ üst Tweaks butonundan açılır. 3 bölüm:

### Varyasyon
- **Tema**: Sade / Saha / Spektrum
- **Aksent**: Mavi / Yeşil / Amber

### Durum
- **İlerleme**: Yeni / Orta / Sınav
- **Çağrı işareti**: Free text (varsayılan `TA2/YOUR`)

### Hızlı git
Her ekrana doğrudan zıplama butonları (test için).

### Tweaks Protokolü
```javascript
// host'a değişiklik gönder
window.parent.postMessage({
  type: '__edit_mode_set_keys',
  edits: { variant: 'spektrum' }
}, '*');

// Tweaks UI'yi göster/gizle (toolbar'dan)
window.addEventListener('message', (e) => {
  if (e.data.type === '__activate_edit_mode') { /* göster */ }
  if (e.data.type === '__deactivate_edit_mode') { /* gizle */ }
});
```

---

## Müfredat Verisi

### `data.js` Yapısı

```javascript
window.CURRICULUM = [
  {
    id: 'b1',
    no: 1,
    kod: 'NATO',
    baslik: 'NATO Alfabesi',
    aciklama: 'Uluslararası fonetik alfabe...',
    glyph: 'A·B·C',
    renk: '#1E3A8A',
    dersler: [
      { id: 'b1l1', baslik: "A'dan G'ye", alt: 'Alpha · Bravo...', sure: 6 },
      { id: 'b1l2', baslik: "H'den N'ye", alt: '...', sure: 6 },
      { id: 'b1l3', baslik: "O'dan U'ya", alt: '...', sure: 6 },
      { id: 'b1l4', baslik: "V'den Z'ye + Rakamlar", alt: '...', sure: 7 },
      { id: 'b1q',  baslik: 'Bölüm Sınavı', alt: '15 soru', sure: 8, sinav: true },
    ],
  },
  // ... 5 bölüm daha
];
```

### Diğer Global Veriler
| Değişken | İçerik |
|---|---|
| `window.NATO` | 26 harf × `[harf, kelime, telaffuz]` |
| `window.QKODES` | 10 Q kodu × `[kod, anlam]` |
| `window.MORSE` | Yaygın mors örnekleri (`SOS`, `CQ`, `73`, vb.) |
| `window.GUNUN_BILGISI` | "Günün bilgisi" havuzu |
| `window.ROZETLER` | 9 rozet (kazanıldı flag'i ile) |

### Bölüm Ders Sayıları
Her bölüm **5 ders** içeriyor (4 öğretme + 1 sınav). Toplam **30 ders**.

---

## Android Port Rehberi

> Mevcut Android projesi: `android_native/` (Kotlin + Jetpack Compose).
> 
> Tam handoff README + Compose örnek kodları için: **`design_handoff_ana_menu/README.md`**

### Hızlı Eşleme

| HTML Component | Compose Eşdeğeri |
|---|---|
| `App` | `MainActivity` + `NavHost` |
| `Home` | `HomeScreen.kt` (mevcut, refactor edilecek) |
| `ChapterSelector` | `Card { Row { IconButton ← / → } }` + dot indicators (Canvas) |
| `ChapterShowcase` | Switch composable: `when(bolumId)` |
| `HomeLessonRow` | `LessonRow` (mevcut, stil güncellenecek) |
| `GrillMeButton` | `FloatingActionButton` |
| `GrillMeModal` | `ModalBottomSheet` (Material 3) |
| `Toast` | `Snackbar` veya `Toast` |
| `BottomNav` | `NavigationBar` (Material 3) |
| `TweaksPanel` | DataStore + `Settings` ekranı |

### Tasarım Token'ları (Compose)

```kotlin
// Theme.kt — Spektrum (varsayılan dark)
val SpektrumColors = darkColorScheme(
    primary       = Color(0xFF5BD9C8),  // teal-cyan aksent
    onPrimary     = Color(0xFF0B1020),
    background    = Color(0xFF0B1020),
    surface       = Color(0xFF11172A),
    surfaceVariant= Color(0xFF0E1426),
    onSurface     = Color(0xFFF5F2E9),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline       = Color(0xFF1F2937),
    error         = Color(0xFFB91C1C),
)

// Bölüm renkleri (curriculum'dan geliyor)
object BolumColors {
    val Nato      = Color(0xFF1E3A8A)
    val QCode     = Color(0xFF0F766E)
    val Elektronik= Color(0xFFB45309)
    val Frekans   = Color(0xFF0369A1)
    val Prosedur  = Color(0xFF15803D)
    val Trac      = Color(0xFF7C2D12)
}
```

### Tipografi (Compose)

```kotlin
// Fontlar res/font/ klasöründe:
//   ibm_plex_mono_regular.ttf
//   ibm_plex_mono_medium.ttf
//   ibm_plex_mono_semibold.ttf
//   ibm_plex_mono_bold.ttf
//   inter_regular.ttf / inter_semibold.ttf / inter_bold.ttf / inter_extrabold.ttf

val MonoFamily = FontFamily(
    Font(R.font.ibm_plex_mono_regular,  FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium,   FontWeight.Medium),
    Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_mono_bold,     FontWeight.Bold),
)

val SansFamily = FontFamily(
    Font(R.font.inter_regular,    FontWeight.Normal),
    Font(R.font.inter_semibold,   FontWeight.SemiBold),
    Font(R.font.inter_bold,       FontWeight.Bold),
    Font(R.font.inter_extrabold,  FontWeight.ExtraBold),
)
```

### Zigzag Path → Compose Canvas
> **NOT:** Zigzag yol artık HTML prototipinde **kaldırıldı** (kullanıcı talebiyle). Yerine bölüm seçici geldi. Eski zigzag tasarımına `design_handoff_ana_menu/prototype/` arşivinde erişilebilir.

### Detaylı Port Adımları
Tüm composable örnekleri için: **`design_handoff_ana_menu/README.md`** dosyasını incele. Her ekran için:
- LazyColumn yapısı
- Spacing/padding değerleri
- Animasyon (pulse, fade) örnekleri
- Canvas çizimleri (Ohm üçgeni, spektrum grafik)

---

## Genişletme Rehberi

### Yeni Bölüm Ekleme

1. **`data.js`**'ye CURRICULUM'a yeni obje ekle:
```javascript
{
  id: 'b7',
  no: 7,
  kod: 'ANTEN',
  baslik: 'Anten Sistemleri',
  aciklama: 'Dipol, Yagi, vertikal antenler...',
  glyph: '↑Y↑',
  renk: '#581C87',
  dersler: [
    { id: 'b7l1', baslik: 'Anten Temelleri', alt: '...', sure: 7 },
    // ...
  ],
}
```

2. **`chapter-pages.jsx`**'e showcase ekle:
```javascript
function ShowcaseAnten({ t, bolum }) {
  return <ShowcaseShell t={t} bolum={bolum} eyebrow="DIPOL · YAGI · VERTİKAL" ...>
    {/* Bölüme özel görsel widget */}
  </ShowcaseShell>;
}

// switch'e ekle
case 'b7': return <ShowcaseAnten t={t} bolum={bolum} />;
```

3. **`screens.jsx`** içinde `generateQuestions()` map'ine soru havuzu ekle:
```javascript
b7: [
  { p: 'Yagi anteni nedir?', opts: [...], a: 0 },
  // ...
]
```

4. **`home.jsx`** içinde `randomGrillQuestion()` pools map'ine de aynı şekilde:
```javascript
b7: [ { p: '...', opts: [...], a: 0 } ]
```

5. **`makeInitialState`** içinde ilerleme senaryolarını güncelle (yeni bölümün her seviyede durumu).

### Yeni Bölüm-İçi Ders Ekleme

Sadece `data.js` içindeki `CURRICULUM[X].dersler` dizisine yeni obje ekle. Ders ID'leri unique olsun.

### Yeni Rozet Ekleme

`data.js`'ye `ROZETLER` dizisine ekle:
```javascript
{ id: 'super_op', isim: 'Süper Operatör', glyph: '★★', aciklama: 'Tüm bölümleri 100% bitir', kazanildi: false }
```

### Yeni Soru Tipi (Sadece-Çoktan-Seçmeli Değil)

Mevcut `Ders` component'i sadece çoktan seçmeli desteklediği için yeni soru tipleri eklemek için `Ders` component'inde `q.type` switch'i kurulup yeni render branch'leri yazılmalı (örn. drag-drop, eşleştirme, fill-in-blank).

---

## Bilinen Sınırlamalar

1. **Persistance yok**: Sayfa yenilenince state Tweaks ayarına resetlenir. Üretimde DataStore/LocalStorage gerekli.

2. **Ses/TTS yok**: NATO telaffuzları yazılı (`/MIKE/`). Mevcut Android projesinde TTS var; port'ta korunmalı.

3. **Bölüm Detay sayfası ana sayfayla aynı içeriği gösteriyor**: Bottom nav "DERSLER" → kütüphaneye gider zaten. `bolum` ekranı şu anda sadece eski derin link akışı için duruyor. Bu kasıtlı bir basitleştirme.

4. **Sınav menüsündeki butonlar henüz işlevsel değil**: Tam deneme/bölüm sınavı akışları stub. Mevcut `ExerciseScreen`'in genişletilmiş hâli kullanılacak.

5. **Profil rozetleri statik**: `data.js`'deki `kazanildi: true/false` flag'leri sabit. Üretimde XP/ders tamamlamaya göre dinamik hesaplanmalı.

6. **Grill Me cevapları XP vermiyor**: Sadece pratik. İstenirse `complete-lesson` reducer'a benzer bir `grill-correct` aksiyonu eklenebilir.

7. **i18n yok**: Tüm metinler Türkçe hardcoded. Çoklu dil için tüm string'ler `locale.js` gibi bir dosyaya çıkarılmalı.

8. **Erişilebilirlik (a11y)**:
   - `aria-label`'lar eksik (özellikle ikon-only butonlarda)
   - Tab focus akışı test edilmedi
   - Renk kontrastı WCAG AA seviyesinde (✓) ama özel test yapılmadı

9. **Performans**: Yeniden render optimizasyonu yok (React.memo, useMemo eksikleri). 30 derste sorun olmaz; 200+ derste profillemeye değer.

---

## Lisans ve Notlar

Bu prototip **iç tasarım çalışmasıdır**. Üretim Android koduna port edilecek.

- "Duolingo gibi" referansı sadece **mekanik/oyunlaştırma yaklaşımına** ait — özgün marka görselleri kullanılmadı.
- NATO/Q kodları/Mors **uluslararası standart**, telif altında değil.
- TRAC mevzuat soruları örnek/eğitim amaçlı — resmi TRAC arşiviyle güncellenmeli.

---

## Hızlı Komut Referansı

```bash
# Aç
open "Telsiz Okulu.html"

# Tweaks
[Sağ üst] → tema/aksent/ilerleme

# Klavye Kısayolu (yok — gerekirse eklenebilir)
# Tarayıcıda yapay:
goTo({ screen: 'lesson', bolumId: 'b1', dersId: 'b1l1' })

# Android handoff paketi
ls design_handoff_ana_menu/
```

---

**Son güncellenme:** Telsiz Okulu prototip v1.2  
**Tasarım yönü:** Aydınlık + Mühendislik (Spektrum dark varsayılan)  
**Hedef platform:** Android (Kotlin + Jetpack Compose)
