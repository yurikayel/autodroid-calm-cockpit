package com.example.autodroid.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Width at which car / landscape layout tokens apply (matches nav rail). */
const val WideLayoutBreakpointDp = 600

data class CarDimensTokens(
    val touchTarget: Dp,
    val targetSpacing: Dp,
    val iconPrimary: Dp,
    val iconSecondary: Dp,
    val iconTertiary: Dp,
    val tileRadius: Dp,
    val heroRadius: Dp,
    val listRowHeight: Dp,
    val railWidth: Dp,
    val contentPadding: Dp,
    val bentoGap: Dp,
    val avatarSize: Dp,
    val energyRingSize: Dp
)

object CarDimens {
    val Wide = CarDimensTokens(
        touchTarget = 76.dp,
        targetSpacing = 24.dp,
        iconPrimary = 44.dp,
        iconSecondary = 36.dp,
        iconTertiary = 24.dp,
        tileRadius = 28.dp,
        heroRadius = 36.dp,
        listRowHeight = 96.dp,
        railWidth = 96.dp,
        contentPadding = 24.dp,
        bentoGap = 16.dp,
        avatarSize = 56.dp,
        energyRingSize = 120.dp
    )

    val Compact = CarDimensTokens(
        touchTarget = 56.dp,
        targetSpacing = 12.dp,
        iconPrimary = 28.dp,
        iconSecondary = 24.dp,
        iconTertiary = 20.dp,
        tileRadius = 20.dp,
        heroRadius = 28.dp,
        listRowHeight = 72.dp,
        railWidth = 72.dp,
        contentPadding = 16.dp,
        bentoGap = 12.dp,
        avatarSize = 44.dp,
        energyRingSize = 96.dp
    )

    fun forWidth(widthDp: Int): CarDimensTokens =
        if (widthDp >= WideLayoutBreakpointDp) Wide else Compact

    // Legacy aliases — wide defaults for shapes / non-composable call sites.
    val TouchTarget = Wide.touchTarget
    val TargetSpacing = Wide.targetSpacing
    val IconPrimary = Wide.iconPrimary
    val IconSecondary = Wide.iconSecondary
    val IconTertiary = Wide.iconTertiary
    val TileRadius = Wide.tileRadius
    val HeroRadius = Wide.heroRadius
    val ListRowHeight = Wide.listRowHeight
    val RailWidth = Wide.railWidth
    val ContentPadding = Wide.contentPadding
    val BentoGap = Wide.bentoGap
}

val LocalCarDimens = staticCompositionLocalOf { CarDimens.Wide }

@Composable
fun rememberCarDimens(): CarDimensTokens {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return remember(widthDp) { CarDimens.forWidth(widthDp) }
}

@Composable
fun isWideLayout(): Boolean =
    LocalConfiguration.current.screenWidthDp >= WideLayoutBreakpointDp
