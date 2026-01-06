package com.kaidendev.rebelioclientandroid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MatrixGreen,
    onPrimary = DeepBlack,
    secondary = MatrixGreenDark,
    onSecondary = DeepBlack,
    tertiary = MatrixGreenTranslucent,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = SurfaceBlack,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = TextPrimary,
    surfaceVariant = CardBlack,
    onSurfaceVariant = TextPrimary,
    outline = MatrixGreenTranslucent
)

@Composable
fun RebelioTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepBlack.toArgb()
            window.navigationBarColor = DeepBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RebelioTypography,
        shapes = RebelioShapes,
        content = content
    )
}
