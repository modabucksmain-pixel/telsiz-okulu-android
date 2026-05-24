package com.telsizokulu.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val SpektrumColorScheme = darkColorScheme(
    primary              = SpektrumAccent,
    onPrimary            = SpektrumBg,
    primaryContainer     = SpektrumAccentTint,
    onPrimaryContainer   = SpektrumAccentHi,
    secondary            = SpektrumStreak,
    onSecondary          = SpektrumBg,
    secondaryContainer   = SpektrumStreakTint,
    onSecondaryContainer = SpektrumOnSurface,
    tertiary             = GoldXP,
    onTertiary           = SpektrumBg,
    background           = SpektrumBg,
    onBackground         = SpektrumOnSurface,
    surface              = SpektrumSurface,
    onSurface            = SpektrumOnSurface,
    surfaceVariant       = SpektrumSurfaceVar,
    onSurfaceVariant     = SpektrumOnSurfaceVar,
    outline              = SpektrumOutline,
    outlineVariant       = SpektrumOutline2,
    error                = Danger,
    onError              = SpektrumOnSurface,
)

@Composable
fun TelsizOkuluTheme(content: @Composable () -> Unit) {
    val colorScheme = SpektrumColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SpektrumBg.toArgb()
            window.navigationBarColor = SpektrumBg.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
}
