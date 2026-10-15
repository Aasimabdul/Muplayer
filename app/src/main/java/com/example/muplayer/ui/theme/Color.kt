package com.example.muplayer.ui.theme

import androidx.compose.ui.graphics.Color

data class AppThemePalette(
    val id: String,
    val name: String,
    val isLight: Boolean,
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
    val inactiveIcon: Color,
    val border: Color,
    val slider: Color,
    val progress: Color,
    val dialog: Color,
    val navigationBar: Color
)

object AppThemes {
    // 1. Navy Blue — Default
    val NavyBlue = AppThemePalette(
        id = "navy_blue",
        name = "Navy Blue",
        isLight = false,
        primary = Color(0xFF142B4A),
        secondary = Color(0xFF244A73),
        background = Color(0xFF101820),
        surface = Color(0xFF1B2938),
        elevatedSurface = Color(0xFF24364A),
        primaryText = Color(0xFFF4F7FC),
        secondaryText = Color(0xFFA0B2C6),
        accent = Color(0xFF4D8DFF),
        inactiveIcon = Color(0xFF6B7F96),
        border = Color(0x334D8DFF),
        slider = Color(0xFF4D8DFF),
        progress = Color(0xFF4D8DFF),
        dialog = Color(0xFF1B2938),
        navigationBar = Color(0xFF1B2938)
    )

    // 2. System Adaptive — Dynamically computed via getSystemAdaptive(isDark)

    // 3. Neon Blue
    val NeonBlue = AppThemePalette(
        id = "neon_blue",
        name = "Neon Blue",
        isLight = false,
        primary = Color(0xFF00A8FF),
        secondary = Color(0xFF0077B6),
        background = Color(0xFF050B14),
        surface = Color(0xFF0D1828),
        elevatedSurface = Color(0xFF14243B),
        primaryText = Color(0xFFF2F8FF),
        secondaryText = Color(0xFF8DA8C8),
        accent = Color(0xFF36D9FF),
        inactiveIcon = Color(0xFF4A6B8F),
        border = Color(0x3336D9FF),
        slider = Color(0xFF36D9FF),
        progress = Color(0xFF36D9FF),
        dialog = Color(0xFF0D1828),
        navigationBar = Color(0xFF0D1828)
    )

    // 4. AMOLED Black
    val AmoledBlack = AppThemePalette(
        id = "amoled_black",
        name = "AMOLED Black",
        isLight = false,
        primary = Color(0xFF141414),
        secondary = Color(0xFF222222),
        background = Color(0xFF000000),
        surface = Color(0xFF080808),
        elevatedSurface = Color(0xFF141414),
        primaryText = Color(0xFFFFFFFF),
        secondaryText = Color(0xFF888888),
        accent = Color(0xFFFFFFFF),
        inactiveIcon = Color(0xFF555555),
        border = Color(0x33FFFFFF),
        slider = Color(0xFFFFFFFF),
        progress = Color(0xFFFFFFFF),
        dialog = Color(0xFF080808),
        navigationBar = Color(0xFF080808)
    )

    // 5. Pure White
    val PureWhite = AppThemePalette(
        id = "pure_white",
        name = "Pure White",
        isLight = true,
        primary = Color(0xFF2858D8),
        secondary = Color(0xFFE2E7F5),
        background = Color(0xFFFAFAFC),
        surface = Color(0xFFFFFFFF),
        elevatedSurface = Color(0xFFF1F3F7),
        primaryText = Color(0xFF16181D),
        secondaryText = Color(0xFF5E6573),
        accent = Color(0xFF2858D8),
        inactiveIcon = Color(0xFF8C95A6),
        border = Color(0x2216181D),
        slider = Color(0xFF2858D8),
        progress = Color(0xFF2858D8),
        dialog = Color(0xFFFFFFFF),
        navigationBar = Color(0xFFFFFFFF)
    )

    // 6. Purple Galaxy
    val PurpleGalaxy = AppThemePalette(
        id = "purple_galaxy",
        name = "Purple Galaxy",
        isLight = false,
        primary = Color(0xFFB18CFF),
        secondary = Color(0xFF7C52D9),
        background = Color(0xFF100B1B),
        surface = Color(0xFF1D1530),
        elevatedSurface = Color(0xFF2B2045),
        primaryText = Color(0xFFF8F3FF),
        secondaryText = Color(0xFFA899C2),
        accent = Color(0xFFD6BFFF),
        inactiveIcon = Color(0xFF635280),
        border = Color(0x33D6BFFF),
        slider = Color(0xFFD6BFFF),
        progress = Color(0xFFD6BFFF),
        dialog = Color(0xFF1D1530),
        navigationBar = Color(0xFF1D1530)
    )

    // 7. Emerald Forest
    val EmeraldForest = AppThemePalette(
        id = "emerald_forest",
        name = "Emerald Forest",
        isLight = false,
        primary = Color(0xFF38D9A9),
        secondary = Color(0xFF209E78),
        background = Color(0xFF0A1713),
        surface = Color(0xFF14251F),
        elevatedSurface = Color(0xFF1F382F),
        primaryText = Color(0xFFF0FFF8),
        secondaryText = Color(0xFF8CB3A3),
        accent = Color(0xFF75F0C8),
        inactiveIcon = Color(0xFF4A7565),
        border = Color(0x3375F0C8),
        slider = Color(0xFF75F0C8),
        progress = Color(0xFF75F0C8),
        dialog = Color(0xFF14251F),
        navigationBar = Color(0xFF14251F)
    )

    // 8. Sunset Amber
    val SunsetAmber = AppThemePalette(
        id = "sunset_amber",
        name = "Sunset Amber",
        isLight = false,
        primary = Color(0xFFFFB547),
        secondary = Color(0xFFC77C1E),
        background = Color(0xFF17100D),
        surface = Color(0xFF291C16),
        elevatedSurface = Color(0xFF3D2A20),
        primaryText = Color(0xFFFFF8F0),
        secondaryText = Color(0xFFC4A897),
        accent = Color(0xFFFFD27D),
        inactiveIcon = Color(0xFF826857),
        border = Color(0x33FFD27D),
        slider = Color(0xFFFFD27D),
        progress = Color(0xFFFFD27D),
        dialog = Color(0xFF291C16),
        navigationBar = Color(0xFF291C16)
    )

    // 9. Crimson Red
    val CrimsonRed = AppThemePalette(
        id = "crimson_red",
        name = "Crimson Red",
        isLight = false,
        primary = Color(0xFFFF5268),
        secondary = Color(0xFFC4283C),
        background = Color(0xFF160C10),
        surface = Color(0xFF26151B),
        elevatedSurface = Color(0xFF3B202A),
        primaryText = Color(0xFFFFF4F6),
        secondaryText = Color(0xFFC29BA4),
        accent = Color(0xFFFF8494),
        inactiveIcon = Color(0xFF805762),
        border = Color(0x33FF8494),
        slider = Color(0xFFFF8494),
        progress = Color(0xFFFF8494),
        dialog = Color(0xFF26151B),
        navigationBar = Color(0xFF26151B)
    )

    // 10. Arctic Cyan
    val ArcticCyan = AppThemePalette(
        id = "arctic_cyan",
        name = "Arctic Cyan",
        isLight = false,
        primary = Color(0xFF42DDF5),
        secondary = Color(0xFF189CB3),
        background = Color(0xFF09151A),
        surface = Color(0xFF12252C),
        elevatedSurface = Color(0xFF1C3742),
        primaryText = Color(0xFFF1FCFF),
        secondaryText = Color(0xFF8BB1BC),
        accent = Color(0xFF8CEEFF),
        inactiveIcon = Color(0xFF4C727D),
        border = Color(0x338CEEFF),
        slider = Color(0xFF8CEEFF),
        progress = Color(0xFF8CEEFF),
        dialog = Color(0xFF12252C),
        navigationBar = Color(0xFF12252C)
    )

    fun getSystemAdaptive(isDark: Boolean): AppThemePalette {
        return if (isDark) {
            AppThemePalette(
                id = "system_adaptive",
                name = "System Adaptive",
                isLight = false,
                primary = Color(0xFF171717),
                secondary = Color(0xFF333333),
                background = Color(0xFF000000),
                surface = Color(0xFF161616),
                elevatedSurface = Color(0xFF222222),
                primaryText = Color(0xFFFFFFFF),
                secondaryText = Color(0xFFA0A0A0),
                accent = Color(0xFFFFFFFF),
                inactiveIcon = Color(0xFF737373),
                border = Color(0x33FFFFFF),
                slider = Color(0xFFFFFFFF),
                progress = Color(0xFFFFFFFF),
                dialog = Color(0xFF161616),
                navigationBar = Color(0xFF161616)
            )
        } else {
            AppThemePalette(
                id = "system_adaptive",
                name = "System Adaptive",
                isLight = true,
                primary = Color(0xFFF5F5F5),
                secondary = Color(0xFFE5E5E5),
                background = Color(0xFFFFFFFF),
                surface = Color(0xFFF5F5F5),
                elevatedSurface = Color(0xFFEAEAEA),
                primaryText = Color(0xFF171717),
                secondaryText = Color(0xFF666666),
                accent = Color(0xFF171717),
                inactiveIcon = Color(0xFF888888),
                border = Color(0x33000000),
                slider = Color(0xFF171717),
                progress = Color(0xFF171717),
                dialog = Color(0xFFF5F5F5),
                navigationBar = Color(0xFFF5F5F5)
            )
        }
    }

    fun getThemePalette(id: String, isSystemDark: Boolean = true): AppThemePalette {
        return when (id) {
            "system_adaptive" -> getSystemAdaptive(isSystemDark)
            "neon_blue" -> NeonBlue
            "amoled", "amoled_black" -> AmoledBlack
            "light", "pure_white" -> PureWhite
            "purple_galaxy", "sunset" -> PurpleGalaxy
            "emerald_forest", "emerald" -> EmeraldForest
            "sunset_amber" -> SunsetAmber
            "crimson_red", "ruby" -> CrimsonRed
            "arctic_cyan" -> ArcticCyan
            else -> NavyBlue
        }
    }
}

// Glass Surface colors
val GlassSurfaceLight = Color(0x1A000000)
val GlassSurfaceDark = Color(0x1AFFFFFF)
val GlassSurface = Color(0x1AFFFFFF)

val GlassBorderLight = Color(0x33000000)
val GlassBorderDark = Color(0x33FFFFFF)
val GlassBorder = Color(0x33FFFFFF)
