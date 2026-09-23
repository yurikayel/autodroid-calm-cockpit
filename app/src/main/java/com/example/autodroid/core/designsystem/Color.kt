package com.example.autodroid.core.designsystem

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Brand seed accents
private val NorthwindBlue = Color(0xFF002B5E)
private val NorthwindRed = Color(0xFFD22630)
private val SummitGraphite = Color(0xFF8A8D8F)
private val SummitAmber = Color(0xFFF2A900)
private val HarborSky = Color(0xFF00B0F0)
private val HarborNavy = Color(0xFF003057)

val NorthwindDarkColors = darkColorScheme(
    primary = Color(0xFF9BB7FF),
    onPrimary = Color(0xFF002B5E),
    primaryContainer = Color(0xFF003F87),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = NorthwindRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8B1018),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFF7DD3FC),
    onTertiary = Color(0xFF00344A),
    tertiaryContainer = Color(0xFF004D68),
    onTertiaryContainer = Color(0xFFC4E7FF),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE2E7EF),
    surface = Color(0xFF0B0F14),
    onSurface = Color(0xFFE2E7EF),
    surfaceVariant = Color(0xFF2A303A),
    onSurfaceVariant = Color(0xFFC3C7D0),
    surfaceContainerLowest = Color(0xFF06090D),
    surfaceContainerLow = Color(0xFF131820),
    surfaceContainer = Color(0xFF171C24),
    surfaceContainerHigh = Color(0xFF21262F),
    surfaceContainerHighest = Color(0xFF2C313A),
    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF43474F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val NorthwindLightColors = lightColorScheme(
    primary = NorthwindBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3F),
    secondary = NorthwindRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF410004),
    tertiary = Color(0xFF006685),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC4E7FF),
    onTertiaryContainer = Color(0xFF001E2B),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF43474F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF2F3FA),
    surfaceContainer = Color(0xFFECEDF4),
    surfaceContainerHigh = Color(0xFFE6E8EF),
    surfaceContainerHighest = Color(0xFFE0E2E9),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC3C7D0),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val SummitDarkColors = darkColorScheme(
    primary = Color(0xFFC8C9CB),
    onPrimary = Color(0xFF2F3133),
    primaryContainer = Color(0xFF454749),
    onPrimaryContainer = Color(0xFFE4E2E4),
    secondary = SummitAmber,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = Color(0xFFFFDF98),
    tertiary = Color(0xFF9CCBFF),
    onTertiary = Color(0xFF003355),
    tertiaryContainer = Color(0xFF004A78),
    onTertiaryContainer = Color(0xFFCFE5FF),
    background = Color(0xFF121314),
    onBackground = Color(0xFFE3E2E3),
    surface = Color(0xFF121314),
    onSurface = Color(0xFFE3E2E3),
    surfaceVariant = Color(0xFF44474A),
    onSurfaceVariant = Color(0xFFC4C7CA),
    surfaceContainerLowest = Color(0xFF0D0E0F),
    surfaceContainerLow = Color(0xFF1A1C1D),
    surfaceContainer = Color(0xFF1E2021),
    surfaceContainerHigh = Color(0xFF292A2B),
    surfaceContainerHighest = Color(0xFF343536),
    outline = Color(0xFF8E9194),
    outlineVariant = Color(0xFF44474A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val SummitLightColors = lightColorScheme(
    primary = Color(0xFF5A5C5E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E2E4),
    onPrimaryContainer = Color(0xFF1B1C1E),
    secondary = Color(0xFF7A5E00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDF98),
    onSecondaryContainer = Color(0xFF261A00),
    tertiary = Color(0xFF00629E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFE5FF),
    onTertiaryContainer = Color(0xFF001D34),
    background = Color(0xFFFCF8F8),
    onBackground = Color(0xFF1C1B1B),
    surface = Color(0xFFFCF8F8),
    onSurface = Color(0xFF1C1B1B),
    surfaceVariant = Color(0xFFE3E2E3),
    onSurfaceVariant = Color(0xFF464647),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF6F3F3),
    surfaceContainer = Color(0xFFF0EDED),
    surfaceContainerHigh = Color(0xFFEAE7E7),
    surfaceContainerHighest = Color(0xFFE4E2E2),
    outline = Color(0xFF777778),
    outlineVariant = Color(0xFFC7C6C7),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val HarborDarkColors = darkColorScheme(
    primary = Color(0xFF7CD0FF),
    onPrimary = Color(0xFF00344B),
    primaryContainer = Color(0xFF004C6B),
    onPrimaryContainer = Color(0xFFC6E7FF),
    secondary = Color(0xFFA9C8E0),
    onSecondary = Color(0xFF0D3447),
    secondaryContainer = Color(0xFF274B5F),
    onSecondaryContainer = Color(0xFFC6E7FF),
    tertiary = Color(0xFFC3C1FF),
    onTertiary = Color(0xFF2A2A6A),
    tertiaryContainer = Color(0xFF414182),
    onTertiaryContainer = Color(0xFFE2DFFF),
    background = Color(0xFF0B1217),
    onBackground = Color(0xFFE1E3E5),
    surface = Color(0xFF0B1217),
    onSurface = Color(0xFFE1E3E5),
    surfaceVariant = Color(0xFF40484E),
    onSurfaceVariant = Color(0xFFC0C7CD),
    surfaceContainerLowest = Color(0xFF060D11),
    surfaceContainerLow = Color(0xFF131A1F),
    surfaceContainer = Color(0xFF171E23),
    surfaceContainerHigh = Color(0xFF22282D),
    surfaceContainerHighest = Color(0xFF2C3338),
    outline = Color(0xFF8A9298),
    outlineVariant = Color(0xFF40484E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

val HarborLightColors = lightColorScheme(
    primary = HarborSky,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC6E7FF),
    onPrimaryContainer = Color(0xFF001E2C),
    secondary = HarborNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC6E7FF),
    onSecondaryContainer = Color(0xFF001E2C),
    tertiary = Color(0xFF595A9C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE2DFFF),
    onTertiaryContainer = Color(0xFF151555),
    background = Color(0xFFF6FAFD),
    onBackground = Color(0xFF171C20),
    surface = Color(0xFFF6FAFD),
    onSurface = Color(0xFF171C20),
    surfaceVariant = Color(0xFFDCE3E9),
    onSurfaceVariant = Color(0xFF40484E),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF0F4F8),
    surfaceContainer = Color(0xFFEAEFF3),
    surfaceContainerHigh = Color(0xFFE4E9ED),
    surfaceContainerHighest = Color(0xFFDFE3E7),
    outline = Color(0xFF70787E),
    outlineVariant = Color(0xFFC0C7CD),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

fun brandColorSchemes(brand: Brand, darkTheme: Boolean) = when (brand) {
    Brand.NORTHWIND -> if (darkTheme) NorthwindDarkColors else NorthwindLightColors
    Brand.SUMMIT -> if (darkTheme) SummitDarkColors else SummitLightColors
    Brand.HARBOR -> if (darkTheme) HarborDarkColors else HarborLightColors
}

/** Accent used by the ambient glow; brand secondary for energy, primary for calm. */
fun Brand.ambientAccent(darkTheme: Boolean): Color = when (this) {
    Brand.NORTHWIND -> if (darkTheme) Color(0xFF3A5F9A) else NorthwindBlue
    Brand.SUMMIT -> if (darkTheme) Color(0xFF8A6A10) else SummitAmber
    Brand.HARBOR -> if (darkTheme) Color(0xFF1A6A90) else HarborSky
}
