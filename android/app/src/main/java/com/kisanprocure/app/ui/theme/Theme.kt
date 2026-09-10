package com.kisanprocure.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val KisanLightColorScheme = lightColorScheme(
    primary               = KisanGreenPrimary,
    onPrimary             = KisanWhite,
    primaryContainer      = KisanMintContainer,
    onPrimaryContainer    = KisanGreenDark,
    secondary             = KisanGreenMedium,
    onSecondary           = KisanWhite,
    secondaryContainer    = KisanSurfaceVariant,
    onSecondaryContainer  = KisanGreenDark,
    tertiary              = KisanAmber,
    onTertiary            = KisanWhite,
    tertiaryContainer     = KisanAmberLight,
    onTertiaryContainer   = KisanTextDark,
    background            = KisanSurfaceLight,
    onBackground          = KisanTextDark,
    surface               = KisanWhite,
    onSurface             = KisanTextDark,
    surfaceVariant        = KisanSurfaceVariant,
    onSurfaceVariant      = KisanTextMuted,
    outline               = KisanBorder,
    outlineVariant        = KisanDivider,
    error                 = KisanError,
    onError               = KisanWhite,
    errorContainer        = KisanErrorBg,
    onErrorContainer      = KisanError
)

private val KisanShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small      = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium     = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large      = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

@Composable
fun KisanTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KisanLightColorScheme,
        typography  = KisanTypography,
        shapes      = KisanShapes,
        content     = content
    )
}
