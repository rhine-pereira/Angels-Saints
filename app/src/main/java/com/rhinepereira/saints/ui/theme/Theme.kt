package com.rhinepereira.saints.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rhinepereira.saints.data.calendar.Vestment

private val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE1F2),
    onPrimaryContainer = IndigoNight,
    secondary = MarigoldInk,
    onSecondary = Color.White,
    secondaryContainer = Marigold,
    onSecondaryContainer = Color(0xFF2A1A00),
    tertiary = Laterite,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6DDD3),
    onTertiaryContainer = Color(0xFF3A1206),
    background = Limewash,
    onBackground = IndigoInk,
    surface = Limewash,
    onSurface = IndigoInk,
    surfaceVariant = Color(0xFFE3E7EE),
    onSurfaceVariant = Color(0xFF4A5372),
    surfaceContainerLowest = Color(0xFFFAFBF9),
    surfaceContainerLow = Color(0xFFEEF1EE),
    surfaceContainer = Color(0xFFE9ECE9),
    surfaceContainerHigh = LimewashDeep,
    surfaceContainerHighest = Color(0xFFDDE2DE),
    outline = Color(0xFF5A6382),
    outlineVariant = Color(0xFFC5CAD6),
    inverseSurface = IndigoNight,
    inverseOnSurface = LampWhite,
    inversePrimary = Color(0xFFC9D3F5)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFC9D3F5),
    onPrimary = Color(0xFF18224A),
    primaryContainer = Color(0xFF2E3B6B),
    onPrimaryContainer = Color(0xFFDCE1F2),
    secondary = MarigoldGlow,
    onSecondary = Color(0xFF2A1A00),
    secondaryContainer = Marigold,
    onSecondaryContainer = Color(0xFF2A1A00),
    tertiary = LateriteGlow,
    onTertiary = Color(0xFF3A1206),
    tertiaryContainer = Color(0xFF5E2716),
    onTertiaryContainer = Color(0xFFF6DDD3),
    background = IndigoNight,
    onBackground = LampWhite,
    surface = IndigoNight,
    onSurface = LampWhite,
    surfaceVariant = Color(0xFF2A3559),
    onSurfaceVariant = Color(0xFFB9B7AE),
    surfaceContainerLowest = Color(0xFF0D1428),
    surfaceContainerLow = Color(0xFF161E3A),
    surfaceContainer = Color(0xFF1A2342),
    surfaceContainerHigh = Color(0xFF222C4E),
    surfaceContainerHighest = Color(0xFF2A3559),
    outline = Color(0xFF8890AE),
    outlineVariant = Color(0xFF343E62),
    inverseSurface = LampWhite,
    inverseOnSurface = IndigoNight,
    inversePrimary = Indigo
)

private val KurusShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Colours outside Material's role set: the garland, the rubric, and vestments. */
@Immutable
data class KurusColors(
    val dark: Boolean,
    val marigold: Color,
    val rubric: Color,
    val trim: Color
) {
    fun vestment(v: Vestment): Color {
        val pair = when (v) {
            Vestment.GREEN -> VestGreen
            Vestment.PURPLE -> VestPurple
            Vestment.WHITE -> VestWhite
            Vestment.RED -> VestRed
            Vestment.ROSE -> VestRose
            Vestment.BLACK -> VestBlack
            Vestment.GOLD -> VestGold
        }
        return if (dark) pair.second else pair.first
    }
}

private val LocalKurus = staticCompositionLocalOf {
    KurusColors(false, Marigold, Laterite, Indigo)
}

object Kurus {
    val colors: KurusColors
        @Composable @ReadOnlyComposable get() = LocalKurus.current
}

@Composable
fun AngelsAndSaintsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic colour is off on purpose: season colour and palette are the identity.
    val kurus = if (darkTheme) {
        KurusColors(true, MarigoldGlow, LateriteGlow, Color(0xFF8890AE))
    } else {
        KurusColors(false, Marigold, Laterite, Indigo)
    }
    CompositionLocalProvider(LocalKurus provides kurus) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = KurusShapes,
            content = content
        )
    }
}
