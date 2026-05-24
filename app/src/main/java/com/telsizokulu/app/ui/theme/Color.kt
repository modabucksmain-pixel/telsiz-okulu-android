package com.telsizokulu.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── LEGACY — existing screens still reference these directly ──────────────
val Blue60          = Color(0xFF2563EB)
val Blue80          = Color(0xFF1D4ED8)
val Blue20          = Color(0xFF93C5FD)
val Purple60        = Color(0xFF7C3AED)
val Purple80        = Color(0xFF6D28D9)
val Purple20        = Color(0xFFC4B5FD)
val Slate950        = Color(0xFF0F172A)
val Slate900        = Color(0xFF1E293B)
val Slate800        = Color(0xFF334155)
val Slate700        = Color(0xFF475569)
val Slate400        = Color(0xFF94A3B8)
val Slate200        = Color(0xFFE2E8F0)
val Slate100        = Color(0xFFF1F5F9)
val SlateWhite      = Color(0xFFF8FAFC)
val GreenSuccess    = Color(0xFF10B981)
val GreenSuccessLight = Color(0xFF6EE7B7)
val AmberWarning    = Color(0xFFF59E0B)
val AmberWarningLight = Color(0xFFFCD34D)
val RedError        = Color(0xFFEF4444)
val RedErrorLight   = Color(0xFFFCA5A5)
val GoldXP          = Color(0xFFEAB308)
val GoldXPLight     = Color(0xFFFDE047)
val BronzeRozet     = Color(0xFFCD7F32)
val SilverRozet     = Color(0xFFC0C0C0)
val GoldRozet       = Color(0xFFFFD700)
val OrangeStreak    = Color(0xFFFF6B00)

// ── SPEKTRUM v2 ───────────────────────────────────────────────────────────
val SpektrumBg           = Color(0xFF0B1020)
val SpektrumSurface      = Color(0xFF11172A)
val SpektrumSurfaceVar   = Color(0xFF0E1426)
val SpektrumOnSurface    = Color(0xFFF5F2E9)
val SpektrumOnSurfaceVar = Color(0xFFD6D2C5)
val SpektrumMuted        = Color(0xFF9CA3AF)
val SpektrumMuted2       = Color(0xFF6B7280)
val SpektrumOutline      = Color(0xFF1F2937)
val SpektrumOutline2     = Color(0xFF2C3849)
val SpektrumAccent       = Color(0xFF5BD9C8)
val SpektrumAccentHi     = Color(0xFF7CEEDC)
val SpektrumAccentTint   = Color(0x1F5BD9C8)
val SpektrumStreak       = Color(0xFFFFB454)
val SpektrumStreakTint   = Color(0x24FFB454)

val Success = Color(0xFF15803D)
val Danger  = Color(0xFFB91C1C)
val Warn    = Color(0xFFA16207)
val Streak  = Color(0xFFDC6803)

// ── CHAPTER COLORS ────────────────────────────────────────────────────────
object BolumColors {
    val Nato       = Color(0xFF1E3A8A)
    val QCode      = Color(0xFF0F766E)
    val Elektronik = Color(0xFFB45309)
    val Frekans    = Color(0xFF0369A1)
    val Prosedur   = Color(0xFF15803D)
    val Trac       = Color(0xFF7C2D12)

    fun fromId(id: String): Color = when (id) {
        "bolum_1" -> Nato
        "bolum_2" -> QCode
        "bolum_3" -> Elektronik
        "bolum_4" -> Frekans
        "bolum_5" -> Prosedur
        "bolum_6" -> Trac
        else      -> Color(0xFF2563EB)
    }

    fun kodFromId(id: String): String = when (id) {
        "bolum_1" -> "NATO"
        "bolum_2" -> "Q-KODU"
        "bolum_3" -> "ELEKTRONİK"
        "bolum_4" -> "FREKANS"
        "bolum_5" -> "PROSEDÜR"
        "bolum_6" -> "TRAC"
        else      -> "???"
    }
}
