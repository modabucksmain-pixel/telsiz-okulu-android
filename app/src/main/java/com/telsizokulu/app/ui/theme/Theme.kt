package com.telsizokulu.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val TelsizDarkColorScheme = darkColorScheme(
    primary = Blue60,
    onPrimary = SlateWhite,
    primaryContainer = Blue80,
    onPrimaryContainer = Blue20,
    secondary = Purple60,
    onSecondary = SlateWhite,
    secondaryContainer = Purple80,
    onSecondaryContainer = Purple20,
    tertiary = GoldXP,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = SlateWhite,
    surface = Slate900,
    onSurface = SlateWhite,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    outlineVariant = Slate800,
    error = RedError,
    onError = SlateWhite,
    errorContainer = RedError.copy(alpha = 0.2f),
    onErrorContainer = RedErrorLight
)

@Composable
fun TelsizOkuluTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = TelsizDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Slate950.toArgb()
            window.navigationBarColor = Slate950.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
