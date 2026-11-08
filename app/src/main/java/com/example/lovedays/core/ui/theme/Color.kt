package com.example.lovedays.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.lovedays.data.datastore.AccentColor

val BrandPink = Color(0xFFFF6F91)
val HeartRed = Color(0xFFFF4D6D)

private data class Palette(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
    val lightBackground: Color,
    val tertiary: Color = BrandPink,
    val darkBackground: Color? = null,
    val lightOnPrimary: Color = Color.White,
)

private fun palette(accent: AccentColor) = when (accent) {
    AccentColor.RED -> Palette(
        Color(0xFFC62828), Color(0xFFFFDAD6), Color(0xFF410002),
        Color(0xFFFFB4AB), Color(0xFF690005), Color(0xFF93000A), Color(0xFFFFDAD6), Color(0xFFFFF8F7),
    )
    AccentColor.PURPLE -> Palette(
        Color(0xFF7B4BB7), Color(0xFFEEDCFF), Color(0xFF2A0053),
        Color(0xFFD8B9FF), Color(0xFF450D86), Color(0xFF5E3399), Color(0xFFEEDCFF), Color(0xFFFEF7FF),
    )
    AccentColor.WHITE -> Palette(
        Color(0xFF5C5C5C), Color(0xFFF1F1F1), Color(0xFF1C1C1C),
        Color(0xFFFFFFFF), Color(0xFF1C1C1C), Color(0xFF3C3C3C), Color(0xFFF5F5F5), Color(0xFFFFFFFF),
        tertiary = Color(0xFF8A8A8A),
    )
    AccentColor.BLACK -> Palette(
        Color(0xFF111111), Color(0xFFE3E3E3), Color(0xFF111111),
        Color(0xFFE6E6E6), Color(0xFF111111), Color(0xFF2B2B2B), Color(0xFFEDEDED), Color(0xFFF7F7F7),
        tertiary = Color(0xFF4A4A4A),
        darkBackground = Color(0xFF000000),
    )
    AccentColor.BLUE -> Palette(
        Color(0xFF2F62C9), Color(0xFFDAE2FF), Color(0xFF001946),
        Color(0xFFB1C5FF), Color(0xFF002C71), Color(0xFF0F4AA8), Color(0xFFDAE2FF), Color(0xFFF9F9FF),
    )
    // PINK và DYNAMIC (dự phòng khi máy không hỗ trợ Dynamic Color)
    else -> Palette(
        Color(0xFFD63F6A), Color(0xFFFFD9E1), Color(0xFF3F001A),
        Color(0xFFFFB1C3), Color(0xFF66002A), Color(0xFF8E1F44), Color(0xFFFFD9E1), Color(0xFFFFF8F9),
    )
}

fun accentPreview(accent: AccentColor): Color = when (accent) {
    AccentColor.PINK -> BrandPink
    AccentColor.RED -> Color(0xFFE53935)
    AccentColor.PURPLE -> Color(0xFF9C6ADE)
    AccentColor.BLUE -> Color(0xFF4A7FE6)
    AccentColor.WHITE -> Color(0xFFFFFFFF)
    AccentColor.BLACK -> Color(0xFF111111)
    AccentColor.DYNAMIC -> Color(0xFF7D8F6A)
}

fun colorSchemeFor(accent: AccentColor, dark: Boolean): ColorScheme {
    val p = palette(accent)
    return if (dark) {
        darkColorScheme(
            primary = p.darkPrimary,
            onPrimary = p.darkOnPrimary,
            primaryContainer = p.darkPrimaryContainer,
            onPrimaryContainer = p.darkOnPrimaryContainer,
            secondary = p.darkPrimary,
            secondaryContainer = p.darkPrimaryContainer,
            onSecondaryContainer = p.darkOnPrimaryContainer,
            tertiary = if (p.tertiary == BrandPink) BrandPink else p.darkPrimaryContainer,
        ).let { if (p.darkBackground != null) it.copy(background = p.darkBackground, surface = p.darkBackground) else it }
    } else {
        lightColorScheme(
            primary = p.primary,
            onPrimary = p.lightOnPrimary,
            primaryContainer = p.primaryContainer,
            onPrimaryContainer = p.onPrimaryContainer,
            secondary = p.primary,
            secondaryContainer = p.primaryContainer,
            onSecondaryContainer = p.onPrimaryContainer,
            tertiary = p.tertiary,
            background = p.lightBackground,
            surface = p.lightBackground,
        )
    }
}
