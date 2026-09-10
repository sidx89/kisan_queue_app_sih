package com.kisanprocure.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KisanLightColorScheme = lightColorScheme(
    primary = KisanGreenPrimary,
    onPrimary = KisanWhite,
    primaryContainer = KisanMintContainer,
    onPrimaryContainer = KisanGreenDark,
    secondary = KisanGreenMedium,
    onSecondary = KisanWhite,
    secondaryContainer = KisanMintContainer,
    onSecondaryContainer = KisanGreenDark,
    tertiary = KisanAmber,
    onTertiary = KisanWhite,
    background = KisanSurfaceLight,
    onBackground = KisanTextDark,
    surface = KisanWhite,
    onSurface = KisanTextDark,
    surfaceVariant = KisanMintContainer,
    onSurfaceVariant = KisanTextMuted,
    outline = KisanBorder,
    error = KisanError,
    onError = KisanWhite
)

@Composable
fun KisanTheme(
    content: @Composable () -> Unit
) {
    // Enforce fresh Light Green & White UI across all devices
    MaterialTheme(
        colorScheme = KisanLightColorScheme,
        content = content
    )
}
