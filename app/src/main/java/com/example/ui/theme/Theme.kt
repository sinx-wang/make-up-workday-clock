package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.data.local.ThemeMode
import com.example.data.local.ThemePalette

@Immutable
data class OneUiSemanticColors(
    val holidayAccent: Color,
    val holidayContainer: Color,
    val lieuAccent: Color,
    val lieuContainer: Color,
    val activeAccent: Color,
    val activeContainer: Color,
    val cardSurface: Color,
    val cardSurfaceElevated: Color,
    val cardBorder: Color,
    val isDark: Boolean
)

val LocalOneUiColors = staticCompositionLocalOf {
    OneUiSemanticColors(
        holidayAccent = HolidayCoralLight,
        holidayContainer = HolidayCoralContainerLight,
        lieuAccent = LieuAmberLight,
        lieuContainer = LieuAmberContainerLight,
        activeAccent = ActiveEmeraldLight,
        activeContainer = ActiveEmeraldContainerLight,
        cardSurface = OneUiLightSurface,
        cardSurfaceElevated = OneUiLightSurfaceHigh,
        cardBorder = OneUiLightOutline,
        isDark = false
    )
}

private data class PaletteTokens(
    val primaryLight: Color,
    val onPrimaryLight: Color,
    val containerLight: Color,
    val onContainerLight: Color,
    val primaryDark: Color,
    val onPrimaryDark: Color,
    val containerDark: Color,
    val onContainerDark: Color,
    val secondaryLight: Color,
    val secondaryDark: Color
)

private fun tokensForPalette(palette: ThemePalette): PaletteTokens = when (palette) {
    ThemePalette.COBALT -> PaletteTokens(
        primaryLight = OneUiBlueLight,
        onPrimaryLight = Color.White,
        containerLight = OneUiBlueContainerLight,
        onContainerLight = OneUiOnBlueContainerLight,
        primaryDark = OneUiBlueDark,
        onPrimaryDark = Color(0xFF001C4D),
        containerDark = OneUiBlueContainerDark,
        onContainerDark = OneUiOnBlueContainerDark,
        secondaryLight = ActiveEmeraldLight,
        secondaryDark = ActiveEmeraldDark
    )
    ThemePalette.EMERALD -> PaletteTokens(
        primaryLight = Color(0xFF0A875E),
        onPrimaryLight = Color.White,
        containerLight = Color(0xFFD5F5E8),
        onContainerLight = Color(0xFF023B28),
        primaryDark = Color(0xFF34D399),
        onPrimaryDark = Color(0xFF002E1E),
        containerDark = Color(0xFF0B3B2B),
        onContainerDark = Color(0xFFBBF7DF),
        secondaryLight = Color(0xFF0284C7),
        secondaryDark = Color(0xFF38BDF8)
    )
    ThemePalette.VIOLET -> PaletteTokens(
        primaryLight = Color(0xFF6642E8),
        onPrimaryLight = Color.White,
        containerLight = Color(0xFFEAE3FF),
        onContainerLight = Color(0xFF230B6B),
        primaryDark = Color(0xFFA78BFA),
        onPrimaryDark = Color(0xFF21075E),
        containerDark = Color(0xFF2B1C5C),
        onContainerDark = Color(0xFFE7DDFF),
        secondaryLight = Color(0xFF2563EB),
        secondaryDark = Color(0xFF60A5FA)
    )
    ThemePalette.CORAL -> PaletteTokens(
        primaryLight = Color(0xFFD93B41),
        onPrimaryLight = Color.White,
        containerLight = Color(0xFFFFE2E3),
        onContainerLight = Color(0xFF5F0A0E),
        primaryDark = Color(0xFFFF7578),
        onPrimaryDark = Color(0xFF470408),
        containerDark = Color(0xFF4A1519),
        onContainerDark = Color(0xFFFFD8D9),
        secondaryLight = Color(0xFFD97706),
        secondaryDark = Color(0xFFFBBF24)
    )
    ThemePalette.AMBER -> PaletteTokens(
        primaryLight = Color(0xFFC47600),
        onPrimaryLight = Color.White,
        containerLight = Color(0xFFFFEED0),
        onContainerLight = Color(0xFF4B2900),
        primaryDark = Color(0xFFFFB74D),
        onPrimaryDark = Color(0xFF3A1E00),
        containerDark = Color(0xFF472B06),
        onContainerDark = Color(0xFFFFE4BA),
        secondaryLight = Color(0xFF0D8A62),
        secondaryDark = Color(0xFF34D399)
    )
    ThemePalette.CYAN -> PaletteTokens(
        primaryLight = Color(0xFF0086A8),
        onPrimaryLight = Color.White,
        containerLight = Color(0xFFD4F3FB),
        onContainerLight = Color(0xFF003645),
        primaryDark = Color(0xFF38D4F5),
        onPrimaryDark = Color(0xFF002B36),
        containerDark = Color(0xFF093947),
        onContainerDark = Color(0xFFC9F2FC),
        secondaryLight = Color(0xFF2563EB),
        secondaryDark = Color(0xFF60A5FA)
    )
    ThemePalette.ROSE -> PaletteTokens(
        primaryLight = Color(0xFFC93B85),
        onPrimaryLight = Color.White,
        containerLight = Color(0xFFFFE0F0),
        onContainerLight = Color(0xFF540831),
        primaryDark = Color(0xFFF472B6),
        onPrimaryDark = Color(0xFF470628),
        containerDark = Color(0xFF4A1230),
        onContainerDark = Color(0xFFFFD6EB),
        secondaryLight = Color(0xFF7C3AED),
        secondaryDark = Color(0xFFA78BFA)
    )
}

private fun buildOneUiDarkScheme(palette: ThemePalette): ColorScheme {
    val t = tokensForPalette(palette)
    val tintedSurface = lerp(OneUiDarkSurface, t.primaryDark, 0.03f)
    val tintedHigh = lerp(OneUiDarkSurfaceHigh, t.primaryDark, 0.05f)
    return darkColorScheme(
        primary = t.primaryDark,
        onPrimary = t.onPrimaryDark,
        primaryContainer = t.containerDark,
        onPrimaryContainer = t.onContainerDark,
        secondary = t.secondaryDark,
        onSecondary = Color(0xFF00261A),
        secondaryContainer = lerp(OneUiDarkSurfaceHigh, t.secondaryDark, 0.22f),
        onSecondaryContainer = lerp(Color.White, t.secondaryDark, 0.25f),
        tertiary = LieuAmberDark,
        onTertiary = Color(0xFF301D00),
        tertiaryContainer = LieuAmberContainerDark,
        onTertiaryContainer = Color(0xFFFFE1B0),
        error = HolidayCoralDark,
        onError = Color(0xFF3E0005),
        errorContainer = HolidayCoralContainerDark,
        onErrorContainer = Color(0xFFFFD6D7),
        background = AmoledBlack,
        onBackground = OneUiDarkTextPrimary,
        surface = tintedSurface,
        onSurface = OneUiDarkTextPrimary,
        surfaceVariant = tintedHigh,
        onSurfaceVariant = OneUiDarkTextSecondary,
        surfaceContainerLowest = AmoledBlack,
        surfaceContainerLow = Color(0xFF0F0F13),
        surfaceContainer = tintedSurface,
        surfaceContainerHigh = tintedHigh,
        surfaceContainerHighest = OneUiDarkSurfaceHighest,
        outline = OneUiDarkOutline,
        outlineVariant = Color(0xFF252630)
    )
}

private fun buildOneUiLightScheme(palette: ThemePalette): ColorScheme {
    val t = tokensForPalette(palette)
    val tintedCanvas = lerp(OneUiLightCanvas, t.primaryLight, 0.035f)
    val tintedHigh = lerp(OneUiLightSurfaceHigh, t.primaryLight, 0.05f)
    return lightColorScheme(
        primary = t.primaryLight,
        onPrimary = t.onPrimaryLight,
        primaryContainer = t.containerLight,
        onPrimaryContainer = t.onContainerLight,
        secondary = t.secondaryLight,
        onSecondary = Color.White,
        secondaryContainer = lerp(Color.White, t.secondaryLight, 0.16f),
        onSecondaryContainer = lerp(Color.Black, t.secondaryLight, 0.55f),
        tertiary = LieuAmberLight,
        onTertiary = Color.White,
        tertiaryContainer = LieuAmberContainerLight,
        onTertiaryContainer = Color(0xFF522B00),
        error = HolidayCoralLight,
        onError = Color.White,
        errorContainer = HolidayCoralContainerLight,
        onErrorContainer = Color(0xFF6B0B0D),
        background = tintedCanvas,
        onBackground = OneUiLightTextPrimary,
        surface = OneUiLightSurface,
        onSurface = OneUiLightTextPrimary,
        surfaceVariant = tintedHigh,
        onSurfaceVariant = OneUiLightTextSecondary,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFF8F9FC),
        surfaceContainer = OneUiLightSurface,
        surfaceContainerHigh = tintedHigh,
        surfaceContainerHighest = Color(0xFFE0E5F0),
        outline = OneUiLightOutline,
        outlineVariant = Color(0xFFE6EAF3)
    )
}

val OneUiShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun HolidayAlarmTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themePalette: ThemePalette = ThemePalette.COBALT,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context).copy(
                    background = AmoledBlack
                )
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> buildOneUiDarkScheme(themePalette)
        else -> buildOneUiLightScheme(themePalette)
    }

    val semanticColors = if (darkTheme) {
        OneUiSemanticColors(
            holidayAccent = HolidayCoralDark,
            holidayContainer = HolidayCoralContainerDark,
            lieuAccent = LieuAmberDark,
            lieuContainer = LieuAmberContainerDark,
            activeAccent = ActiveEmeraldDark,
            activeContainer = ActiveEmeraldContainerDark,
            cardSurface = colorScheme.surface,
            cardSurfaceElevated = colorScheme.surfaceVariant,
            cardBorder = Color(0xFF262732),
            isDark = true
        )
    } else {
        OneUiSemanticColors(
            holidayAccent = HolidayCoralLight,
            holidayContainer = HolidayCoralContainerLight,
            lieuAccent = LieuAmberLight,
            lieuContainer = LieuAmberContainerLight,
            activeAccent = ActiveEmeraldLight,
            activeContainer = ActiveEmeraldContainerLight,
            cardSurface = colorScheme.surface,
            cardSurfaceElevated = colorScheme.surfaceVariant,
            cardBorder = Color(0xFFE5EAF3),
            isDark = false
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(LocalOneUiColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = OneUiShapes,
            content = content
        )
    }
}
