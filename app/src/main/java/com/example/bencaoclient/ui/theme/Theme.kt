package com.example.bencaoclient.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize

private val DarkColorScheme = darkColorScheme(
    primary = BencaoPrimaryDark,
    onPrimary = BencaoOnPrimaryDark,
    primaryContainer = BencaoPrimaryContainerDark,
    onPrimaryContainer = BencaoOnPrimaryContainerDark,
    secondary = BencaoSecondaryDark,
    onSecondary = BencaoOnSecondaryDark,
    secondaryContainer = BencaoSecondaryContainerDark,
    onSecondaryContainer = BencaoOnSecondaryContainerDark,
    tertiary = BencaoTertiaryDark,
    onTertiary = BencaoOnTertiaryDark,
    tertiaryContainer = BencaoTertiaryContainerDark,
    onTertiaryContainer = BencaoOnTertiaryContainerDark,
    background = BencaoBackgroundDark,
    onBackground = BencaoOnBackgroundDark,
    surface = BencaoSurfaceDark,
    onSurface = BencaoOnSurfaceDark,
    surfaceVariant = BencaoSurfaceVariantDark,
    onSurfaceVariant = BencaoOnSurfaceVariantDark,
    outline = BencaoOutlineDark,
    outlineVariant = BencaoOutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = BencaoGreen,
    onPrimary = BencaoGreenOn,
    primaryContainer = BencaoGreenContainer,
    onPrimaryContainer = BencaoGreenOnContainer,
    secondary = BencaoSecondary,
    onSecondary = BencaoSecondaryOn,
    secondaryContainer = BencaoSecondaryContainer,
    onSecondaryContainer = BencaoSecondaryOnContainer,
    tertiary = BencaoTertiary,
    onTertiary = BencaoTertiaryOn,
    tertiaryContainer = BencaoTertiaryContainer,
    onTertiaryContainer = BencaoTertiaryOnContainer,
    background = BencaoBackground,
    onBackground = BencaoOnBackground,
    surface = BencaoSurface,
    onSurface = BencaoOnSurface,
    surfaceVariant = BencaoSurfaceVariant,
    onSurfaceVariant = BencaoOnSurfaceVariant,
    outline = BencaoOutline,
    outlineVariant = BencaoOutlineVariant
)

@Composable
fun BencaoclientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        // 统一为所有页面提供随主题变化的“窗口底色”，避免某些页面未包 Scaffold 时回退成白底
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}