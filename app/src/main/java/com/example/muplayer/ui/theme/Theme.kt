package com.example.muplayer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun AppThemePalette.toColorScheme(): ColorScheme {
    return if (isLight) {
        lightColorScheme(
            primary = accent,
            secondary = secondary,
            background = background,
            surface = surface,
            surfaceVariant = elevatedSurface,
            onBackground = primaryText,
            onSurface = primaryText,
            onSurfaceVariant = secondaryText,
            outline = border,
            outlineVariant = border
        )
    } else {
        darkColorScheme(
            primary = accent,
            secondary = secondary,
            background = background,
            surface = surface,
            surfaceVariant = elevatedSurface,
            onBackground = primaryText,
            onSurface = primaryText,
            onSurfaceVariant = secondaryText,
            outline = border,
            outlineVariant = border
        )
    }
}

val LocalAppThemePalette = staticCompositionLocalOf { AppThemes.NavyBlue }

@Composable
fun MuPlayerTheme(
    themeName: String = "navy_blue",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val palette = remember(themeName, isSystemDark) {
        AppThemes.getThemePalette(themeName, isSystemDark)
    }
    val colorScheme = remember(palette) {
        palette.toColorScheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = palette.isLight
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = palette.isLight
        }
    }

    CompositionLocalProvider(
        LocalAppThemePalette provides palette
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
