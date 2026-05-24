package com.telsizokulu.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Replace with FontFamily(Font(R.font.ibm_plex_mono_*)) when TTF files are added to res/font/
val MonoFamily: FontFamily = FontFamily.Monospace
val SansFamily: FontFamily = FontFamily.Default

val AppTypography = Typography(
    displayLarge  = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Bold,      fontSize = 28.sp, lineHeight = 36.sp),
    headlineLarge = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Bold,      fontSize = 24.sp, lineHeight = 32.sp),
    headlineMedium= TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 20.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 24.sp),
    titleLarge    = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp, lineHeight = 24.sp),
    titleMedium   = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium   = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Medium,    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall    = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Medium,    fontSize = 10.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

// ── Custom semantic TextStyles (v2 design) ────────────────────────────────

val EyebrowMono = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.Bold,
    fontSize = 10.5.sp, letterSpacing = 1.6.sp,
)
val MutedMono = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.SemiBold,
    fontSize = 10.sp, letterSpacing = 1.4.sp,
)
val NumeralMono = TextStyle(
    fontFamily = MonoFamily, fontWeight = FontWeight.Bold,
    fontSize = 22.sp, letterSpacing = (-0.5).sp,
)
val ScreenTitle = TextStyle(
    fontFamily = SansFamily, fontWeight = FontWeight.ExtraBold,
    fontSize = 22.sp, letterSpacing = (-0.4).sp,
)
val ChapterName = TextStyle(
    fontFamily = SansFamily, fontWeight = FontWeight.ExtraBold,
    fontSize = 18.sp, letterSpacing = (-0.3).sp,
)
val LessonTitle = TextStyle(
    fontFamily = SansFamily, fontWeight = FontWeight.Bold,
    fontSize = 14.sp, letterSpacing = (-0.2).sp,
)
val BodyDefault = TextStyle(
    fontFamily = SansFamily, fontWeight = FontWeight.Normal,
    fontSize = 13.5.sp, lineHeight = 19.sp,
)
val BodySmall = TextStyle(
    fontFamily = SansFamily, fontWeight = FontWeight.Normal,
    fontSize = 11.5.sp, lineHeight = 16.sp,
)
