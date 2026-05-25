# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Telsiz Okulu is a native Android port of a Turkish radio operator training web application. It's a gamified educational app for learning radio communication (NATO phonetic alphabet, Q codes, electronics, frequency bands, radio procedures). Built with Kotlin + Jetpack Compose + Material Design 3.

## Build Commands

```bash
# Debug build
./gradlew :app:assembleDebug

# Release build
./gradlew :app:assembleRelease

# Install on connected device/emulator
./gradlew :app:installDebug

# Run unit tests
./gradlew :app:testDebug

# Run a single test class
./gradlew :app:testDebug --tests "com.telsizokulu.app.ExampleUnitTest"

# Run instrumented tests
./gradlew :app:connectedAndroidTest

# Lint
./gradlew :app:lint

# Clean
./gradlew clean
```

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.11.00) + Material Design 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Compose Navigation 2.8.5 |
| Persistence | Jetpack DataStore (Preferences) |
| Data | Gson — parses JSON from `assets/data/` at runtime |
| Audio | Android SoundPool + TextToSpeech |
| Images | Coil 2.7.0 with SVG decoder |
| Async | Kotlin Coroutines 1.9.0 |
| Min/Target SDK | 26 / 35 |

## Architecture

### Package Structure

```
com.telsizokulu.app/
├── MainActivity.kt              # Entry point — EdgeToEdge + TelsizOkuluTheme
├── TelsizOkuluApp.kt            # Application class — Coil ImageLoader config
├── data/
│   ├── model/
│   │   ├── Models.kt            # Curriculum, Bolum, AltBolum, Ders, Kart, Egzersiz, etc.
│   │   └── ProgressModels.kt    # KullaniciIlerleme, BolumIlerleme, KartDurumu, etc.
│   └── repository/
│       ├── CurriculumRepository.kt  # Loads + parses JSON from assets; injects exercise types
│       └── ProgressRepository.kt    # DataStore-backed user progress; XP, streak, level
├── engine/
│   ├── AudioEngine.kt           # SoundPool (squelch, roger beep, correct/wrong) + TTS
│   ├── RadioSfx.kt              # Procedural PCM sound generator (no asset files needed) — squelch, static bed, roger beep, correct/wrong tones
│   ├── ExerciseEngine.kt        # Filters, shuffles, and does weighted selection of exercises
│   └── GamificationEngine.kt   # XP table, level names, badge award conditions
└── ui/
    ├── navigation/
    │   ├── Screen.kt            # Sealed class with all route definitions
    │   └── AppNavGraph.kt       # NavHost wiring all screens to routes
    ├── screens/
    │   ├── HomeScreen.kt        # Chapter map, XP/Streak/Badges overview
    │   ├── BolumScreen.kt       # Chapter detail — sub-chapters and lessons
    │   ├── ExerciseScreen.kt    # Hosts lesson, exercise, exam, and practice modes
    │   ├── KutuphaneScreen.kt   # Theory card library browser
    │   ├── SinavMenuScreen.kt   # Exam hub — 5 exam type rows + T-90 hero card (some rows stub onClick={})
    │   └── ProfileScreen.kt     # User stats, badges, JSON import/export
    ├── components/
    │   ├── GrillMeBottomSheet.kt  # Quick-drill modal; hardcoded question pools (NOT from curriculum JSON)
    │   ├── OnboardingOverlay.kt   # First-launch walkthrough overlay
    │   ├── TelsizBottomNav.kt     # Bottom navigation bar
    │   ├── ChapterSelector.kt     # Chapter picker sheet
    │   ├── ChapterShowcase.kt     # Hero chapter display on HomeScreen
    │   ├── HomeLessonRow.kt       # Lesson row in home chapter list
    │   ├── SpectrumBackground.kt  # Animated spectrum waveform canvas background
    │   ├── CompactStatsStrip.kt   # XP/streak/badge summary strip
    │   ├── StatChip.kt            # Single stat chip (XP / streak / level)
    │   ├── BadgeIcon.kt           # Badge display with glow
    │   └── NatoMonogram.kt        # NATO phonetic letter monogram
    ├── viewmodel/
    │   ├── HomeViewModel.kt     # Loads curriculum + progress for HomeScreen
    │   └── ExerciseViewModel.kt # Drives exercise/exam sessions; owns EgzersizOturumu state
    └── theme/
        ├── Color.kt             # SpektrumBg (#0B1020), BolumColors object, legacy Slate/Blue/Purple tokens
        ├── Type.kt              # MonoFamily, SansFamily; named styles: EyebrowMono, LessonTitle, ChapterName, NumeralMono, BodySmall, MutedMono
        ├── GlowModifiers.kt     # neonGlow() Modifier extension — colored shadow (API 28+) + accent border
        └── Theme.kt             # TelsizOkuluTheme composable
```

### Data Flow

All content is loaded from JSON files in `app/src/main/assets/data/` at startup by `CurriculumRepository`. There is no network layer. User progress is persisted via `ProgressRepository` using Jetpack DataStore.

**Asset files:**
- `curriculum.json` — chapter/sub-chapter/lesson structure
- `nato.json`, `qcodes.json`, `elektronik.json`, `bantlar.json`, `prosedurler.json` — per-chapter content
- `sinav_sorulari.json` — general exam question pool
- `rozetler.json` — badge definitions

### Dependency Injection

No DI framework. `CurriculumRepository`, `ProgressRepository`, and `AudioEngine` are instantiated manually in `AppNavGraph` and passed into ViewModels via `ViewModelProvider.Factory` subclasses defined at the bottom of each ViewModel file.

### Exercise Session Flow

`ExerciseViewModel` is initialized with a `mod` string that determines what content to load:

| mod | Description |
|-----|-------------|
| `"ders"` | Lesson — theory cards first (flip cards), then exercises |
| `"alt_bolum"` | Sub-chapter exam — must score ≥ `gecmePuani` to unlock next |
| `"bolum"` | Chapter exam — scored against chapter's `gecmeSinavi.gecmePuani` |
| `"genel"` | General exam — pulls from all 6 content files, 50 questions, pass = 75 |
| `"pratik"` | Practice — weighted toward cards with `yanlis > 0` |

Lesson mode (`"ders"`) first enters `teoriModu = true` (flip-card phase showing `teoriKartlari`). After all theory cards are reviewed, `teoriModu` flips to `false` and exercises begin. Theory card audio: if `Kart.ses` is set, plays the sound asset; otherwise calls TTS (`en-US` for `nato_*` cards, `tr-TR` otherwise).

### Exercise Types

Exercises are defined in the JSON files with a `type` field:
- `coktan_secmeli` — Multiple choice
- `dogru_yanlis` — True/False
- `bosluk_doldur` — Fill-in-the-blank
- `sayisal` / `hesapla` — Numeric answers (±5% tolerance)
- `dinle_sec` — Listen & select (audio)
- `eslestir` / `eslestime` / `sirala` — Matching/ordering (graded by ExerciseScreen, passes `"dogru"` string)
- `kelime_hece` — Word syllabication
- `hizli_tur` — Speed round
- `harfi_yaz` / `kodu_yaz` — Write the letter/code (uses `gosterilen` field as the prompt)

### Progress State Machine

`BolumIlerleme.durum`: `"kilitli"` → `"devam_ediyor"` → `"tamamlandi"`
`AltBolumIlerleme.durum`: same values
`SinavIlerleme.durum`: `"kilitli"` → `"acik"` → `"tamamlandi"`

`KartDurum.durum` drives weighted exercise selection in `ExerciseEngine.agirlikliKaristir()`:
- `"ogrendim"` → weight 1
- `"ogreniyor"` → weight 3
- `"ogrenmedim"` → weight 5
- (unseen) → weight 4

### Key Gotchas

**Gson null bypass on `Egzersiz`:** Gson ignores Kotlin default values for non-null `String` fields and injects `null` at runtime. `CurriculumRepository.injectTipVeOnarim()` casts every String field to nullable and re-assigns safe defaults. Any new non-null String fields added to `Egzersiz` must be added to this fix block too.

**`Egzersiz.tip` not in JSON:** The `tip` field is NOT read from JSON. It is injected from the `Map<String, List<Egzersiz>>` key by `injectTipVeOnarim()`. If `tip` is empty after that (e.g. JSON has the field), the map key wins.

**Answer comparison normalizes Turkish characters:** `cevapDogruMu()` in `ExerciseViewModel` strips diacritics (`ğ→g`, `ü→u`, `ş→s`, `ı→i`, `ö→o`, `ç→c`) and removes whitespace/hyphens before comparing. Matching/ordering types bypass this — they receive `"dogru"` or `"yanlis"` directly from the UI.

**`dinle_sec` distractors:** Built at two places — in `CurriculumRepository.injectTipVeOnarim()` at load time, and again in `ExerciseEngine.dersEgzersizleriOlustur()` when rescoping to lesson card IDs. The engine version overwrites distractors if the lesson has ≥4 cards.

### Sound Assets

`RadioSfx` generates all UI sound effects (squelch, roger beep, correct/wrong, step-up, chapter complete) procedurally via PCM — no `.wav` files required for SFX. `AudioEngine` still loads `.wav` files from `app/src/main/assets/sounds/` via SoundPool as an alternative path; if those files are absent the app still works.

NATO audio files (e.g. `alpha.mp3`) go in `app/src/main/assets/audio/nato/`. The `Kart.ses` field stores the web path (e.g. `/static/audio/nato/alpha.mp3`); `CurriculumRepository.normalizeSes()` strips it to just the filename stem (`alpha`). TTS is used as fallback when the audio asset is absent.

### Theme and Colors

Two color layers exist in `Color.kt`:
- **Legacy tokens** (`Blue60`, `Slate950`, `GreenSuccess`, etc.) — still referenced by some screens; do not remove.
- **Spektrum v2 tokens** (`SpektrumBg`, `SpektrumSurface`, `SpektrumAccent`, `SpektrumStreak`, etc.) — new dark theme palette used by all post-v2 screens.

`BolumColors` maps `bolum_1`–`bolum_6` to per-chapter accent colors (`Nato`, `QCode`, `Elektronik`, `Frekans`, `Prosedur`, `Trac`). Use `BolumColors.fromId(id)` and `BolumColors.kodFromId(id)` rather than hardcoding colors or labels in new screens.

`neonGlow()` in `GlowModifiers.kt` applies colored shadow + accent border. On API 26/27 the shadow color is ignored (elevation renders grey); the border still shows.

### GrillMe Quick Drill

`GrillMeBottomSheet` in `components/GrillMeBottomSheet.kt` serves quick 4-choice questions. Question pools are **hardcoded** in `generateGrillQuestion()` — they are not loaded from curriculum JSON. Adding new questions requires editing that function directly.
