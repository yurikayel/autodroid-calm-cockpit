package com.example.autodroid.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

val LocalBrand = compositionLocalOf { Brand.NORTHWIND }

/** When true, decorative motion damps and secondary tiles collapse. */
val LocalFocusMode = staticCompositionLocalOf { false }

private val AutoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(CarDimens.TileRadius),
    extraLarge = RoundedCornerShape(CarDimens.HeroRadius)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AutoDroidTheme(
    brand: Brand = Brand.NORTHWIND,
    darkTheme: Boolean = isSystemInDarkTheme(),
    focusMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = brandColorSchemes(brand, darkTheme)
    val motion = if (focusMode) MotionScheme.standard() else MotionScheme.expressive()
    val dimens = CarDimens.forWidth(LocalConfiguration.current.screenWidthDp)

    CompositionLocalProvider(
        LocalBrand provides brand,
        LocalFocusMode provides focusMode,
        LocalCarDimens provides dimens
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = motion,
            shapes = AutoShapes,
            typography = AutoDroidTypography,
            content = content
        )
    }
}
