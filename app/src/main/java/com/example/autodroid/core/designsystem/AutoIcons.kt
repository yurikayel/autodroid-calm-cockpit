package com.example.autodroid.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Subset of Material Symbols used by AutoDroid — avoids the discontinued icons-extended AAR. */
object AutoIcons {
    val Home: ImageVector by lazy {
        ImageVector.Builder(
            name = "Home",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 20f)
                verticalLineTo(14f)
                horizontalLineTo(14f)
                verticalLineTo(20f)
                horizontalLineTo(19f)
                verticalLineTo(12f)
                horizontalLineTo(22f)
                lineTo(12f, 3f)
                lineTo(2f, 12f)
                horizontalLineTo(5f)
                verticalLineTo(20f)
                close()
            }
        }.build()
    }

    val Radio: ImageVector by lazy {
        ImageVector.Builder(
            name = "Radio",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3.24f, 6.15f)
                lineTo(8.46f, 0.93f)
                lineTo(9.87f, 2.34f)
                lineTo(4.66f, 7.55f)
                close()
                moveTo(20f, 8f)
                horizontalLineTo(4f)
                curveTo(2.9f, 8f, 2f, 8.9f, 2f, 10f)
                verticalLineTo(20f)
                curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
                horizontalLineTo(20f)
                curveTo(21.1f, 22f, 22f, 21.1f, 22f, 20f)
                verticalLineTo(10f)
                curveTo(22f, 8.9f, 21.1f, 8f, 20f, 8f)
                close()
                moveTo(7f, 20f)
                curveTo(5.34f, 20f, 4f, 18.66f, 4f, 17f)
                curveTo(4f, 15.34f, 5.34f, 14f, 7f, 14f)
                curveTo(8.66f, 14f, 10f, 15.34f, 10f, 17f)
                curveTo(10f, 18.66f, 8.66f, 20f, 7f, 20f)
                close()
                moveTo(20f, 16f)
                horizontalLineTo(12f)
                verticalLineTo(14f)
                horizontalLineTo(20f)
                verticalLineTo(16f)
                close()
                moveTo(20f, 12f)
                horizontalLineTo(12f)
                verticalLineTo(10f)
                horizontalLineTo(20f)
                verticalLineTo(12f)
                close()
            }
        }.build()
    }

    val Mic: ImageVector by lazy {
        ImageVector.Builder(
            name = "Mic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 14f)
                curveTo(13.66f, 14f, 15f, 12.66f, 15f, 11f)
                verticalLineTo(5f)
                curveTo(15f, 3.34f, 13.66f, 2f, 12f, 2f)
                curveTo(10.34f, 2f, 9f, 3.34f, 9f, 5f)
                verticalLineTo(11f)
                curveTo(9f, 12.66f, 10.34f, 14f, 12f, 14f)
                close()
                moveTo(17.91f, 11f)
                curveTo(17.91f, 14.17f, 15.29f, 16.67f, 12.21f, 16.97f)
                verticalLineTo(20f)
                horizontalLineTo(11.79f)
                verticalLineTo(16.97f)
                curveTo(8.71f, 16.67f, 6.09f, 14.17f, 6.09f, 11f)
                horizontalLineTo(4.09f)
                curveTo(4.09f, 14.79f, 7.03f, 17.92f, 10.71f, 18.41f)
                verticalLineTo(22f)
                horizontalLineTo(13.29f)
                verticalLineTo(18.41f)
                curveTo(16.97f, 17.92f, 19.91f, 14.79f, 19.91f, 11f)
                close()
            }
        }.build()
    }

    val VolumeUp: ImageVector by lazy {
        ImageVector.Builder(
            name = "VolumeUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 9f)
                verticalLineTo(15f)
                horizontalLineTo(7f)
                lineTo(12f, 20f)
                verticalLineTo(4f)
                lineTo(7f, 9f)
                horizontalLineTo(3f)
                close()
                moveTo(16.5f, 12f)
                curveTo(16.5f, 10.23f, 15.48f, 8.71f, 14f, 7.97f)
                verticalLineTo(16.02f)
                curveTo(15.48f, 15.29f, 16.5f, 13.77f, 16.5f, 12f)
                close()
                moveTo(14f, 3.23f)
                verticalLineTo(5.29f)
                curveTo(16.89f, 6.15f, 19f, 8.83f, 19f, 12f)
                curveTo(19f, 15.17f, 16.89f, 17.85f, 14f, 18.71f)
                verticalLineTo(20.77f)
                curveTo(18.01f, 19.86f, 21f, 16.28f, 21f, 12f)
                curveTo(21f, 7.72f, 18.01f, 4.14f, 14f, 3.23f)
                close()
            }
        }.build()
    }

    val Palette: ImageVector by lazy {
        ImageVector.Builder(
            name = "Palette",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 22f)
                curveTo(6.49f, 22f, 2f, 17.51f, 2f, 12f)
                curveTo(2f, 6.49f, 6.49f, 2f, 12f, 2f)
                curveTo(17.51f, 2f, 22f, 6.49f, 22f, 12f)
                curveTo(22f, 13.66f, 20.66f, 15f, 19f, 15f)
                horizontalLineTo(17.5f)
                curveTo(16.12f, 15f, 15f, 16.12f, 15f, 17.5f)
                curveTo(15f, 18.16f, 15.26f, 18.76f, 15.68f, 19.19f)
                curveTo(16.11f, 19.62f, 16.11f, 20.32f, 15.68f, 20.75f)
                curveTo(15.36f, 21.07f, 14.93f, 21.25f, 14.5f, 21.25f)
                curveTo(13.12f, 21.75f, 12.56f, 22f, 12f, 22f)
                close()
                moveTo(6.5f, 12f)
                curveTo(7.33f, 12f, 8f, 11.33f, 8f, 10.5f)
                curveTo(8f, 9.67f, 7.33f, 9f, 6.5f, 9f)
                curveTo(5.67f, 9f, 5f, 9.67f, 5f, 10.5f)
                curveTo(5f, 11.33f, 5.67f, 12f, 6.5f, 12f)
                close()
                moveTo(9.5f, 8f)
                curveTo(10.33f, 8f, 11f, 7.33f, 11f, 6.5f)
                curveTo(11f, 5.67f, 10.33f, 5f, 9.5f, 5f)
                curveTo(8.67f, 5f, 8f, 5.67f, 8f, 6.5f)
                curveTo(8f, 7.33f, 8.67f, 8f, 9.5f, 8f)
                close()
                moveTo(14.5f, 8f)
                curveTo(15.33f, 8f, 16f, 7.33f, 16f, 6.5f)
                curveTo(16f, 5.67f, 15.33f, 5f, 14.5f, 5f)
                curveTo(13.67f, 5f, 13f, 5.67f, 13f, 6.5f)
                curveTo(13f, 7.33f, 13.67f, 8f, 14.5f, 8f)
                close()
                moveTo(17.5f, 12f)
                curveTo(18.33f, 12f, 19f, 11.33f, 19f, 10.5f)
                curveTo(19f, 9.67f, 18.33f, 9f, 17.5f, 9f)
                curveTo(16.67f, 9f, 16f, 9.67f, 16f, 10.5f)
                curveTo(16f, 11.33f, 16.67f, 12f, 17.5f, 12f)
                close()
            }
        }.build()
    }

    val SkipPrevious: ImageVector by lazy {
        ImageVector.Builder(
            name = "SkipPrevious",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 6f)
                horizontalLineTo(8f)
                verticalLineTo(18f)
                horizontalLineTo(6f)
                close()
                moveTo(9.5f, 12f)
                lineTo(18f, 18f)
                verticalLineTo(6f)
                close()
            }
        }.build()
    }

    val SkipNext: ImageVector by lazy {
        ImageVector.Builder(
            name = "SkipNext",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 18f)
                lineTo(14.5f, 12f)
                lineTo(6f, 6f)
                verticalLineTo(18f)
                close()
                moveTo(16f, 6f)
                horizontalLineTo(18f)
                verticalLineTo(18f)
                horizontalLineTo(16f)
                close()
            }
        }.build()
    }

    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 8f)
                horizontalLineTo(17f)
                verticalLineTo(6f)
                curveTo(17f, 3.24f, 14.76f, 1f, 12f, 1f)
                curveTo(9.24f, 1f, 7f, 3.24f, 7f, 6f)
                verticalLineTo(8f)
                horizontalLineTo(6f)
                curveTo(4.9f, 8f, 4f, 8.9f, 4f, 10f)
                verticalLineTo(20f)
                curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
                horizontalLineTo(18f)
                curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
                verticalLineTo(10f)
                curveTo(20f, 8.9f, 19.1f, 8f, 18f, 8f)
                close()
                moveTo(12f, 17f)
                curveTo(10.9f, 17f, 10f, 16.1f, 10f, 15f)
                curveTo(10f, 13.9f, 10.9f, 13f, 12f, 13f)
                curveTo(13.1f, 13f, 14f, 13.9f, 14f, 15f)
                curveTo(14f, 16.1f, 13.1f, 17f, 12f, 17f)
                close()
                moveTo(9f, 8f)
                verticalLineTo(6f)
                curveTo(9f, 4.34f, 10.34f, 3f, 12f, 3f)
                curveTo(13.66f, 3f, 15f, 4.34f, 15f, 6f)
                verticalLineTo(8f)
                close()
            }
        }.build()
    }

    val Tune: ImageVector by lazy {
        ImageVector.Builder(
            name = "Tune",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 17f)
                verticalLineTo(19f)
                horizontalLineTo(9f)
                verticalLineTo(17f)
                horizontalLineTo(3f)
                close()
                moveTo(3f, 5f)
                verticalLineTo(7f)
                horizontalLineTo(13f)
                verticalLineTo(5f)
                horizontalLineTo(3f)
                close()
                moveTo(13f, 21f)
                verticalLineTo(19f)
                horizontalLineTo(21f)
                verticalLineTo(17f)
                horizontalLineTo(13f)
                verticalLineTo(15f)
                horizontalLineTo(11f)
                verticalLineTo(21f)
                horizontalLineTo(13f)
                close()
                moveTo(7f, 9f)
                verticalLineTo(11f)
                horizontalLineTo(3f)
                verticalLineTo(13f)
                horizontalLineTo(7f)
                verticalLineTo(15f)
                horizontalLineTo(9f)
                verticalLineTo(9f)
                horizontalLineTo(7f)
                close()
                moveTo(21f, 13f)
                verticalLineTo(11f)
                horizontalLineTo(11f)
                verticalLineTo(13f)
                horizontalLineTo(21f)
                close()
                moveTo(15f, 9f)
                horizontalLineTo(17f)
                verticalLineTo(7f)
                horizontalLineTo(21f)
                verticalLineTo(5f)
                horizontalLineTo(17f)
                verticalLineTo(3f)
                horizontalLineTo(15f)
                verticalLineTo(9f)
                close()
            }
        }.build()
    }

    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 11f)
                horizontalLineTo(7.83f)
                lineTo(13.42f, 5.41f)
                lineTo(12f, 4f)
                lineTo(4f, 12f)
                lineTo(12f, 20f)
                lineTo(13.41f, 18.59f)
                lineTo(7.83f, 13f)
                horizontalLineTo(20f)
                close()
            }
        }.build()
    }
}
