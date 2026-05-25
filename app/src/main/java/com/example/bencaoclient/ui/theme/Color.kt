package com.example.bencaoclient.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Bencao (herbal) palette.
 *
 * Notes:
 * - Avoid pure black/white for a softer, natural feel.
 * - Keep contrast strong enough for readability.
 */
/** 品牌主色（与首页草绿块、顶栏一致）；其上文字/图标请用 [BencaoGreenOn] 或 Material3 `onPrimary`。 */
val BencaoGreen = Color(0xFF5DB11E)
/** 亮色主题下置于 [BencaoGreen] 上的内容色 */
val BencaoGreenOn = Color(0xFFFFFFFF)
val BencaoGreenContainer = Color(0xFFE3F4D4)
val BencaoGreenOnContainer = Color(0xFF1A3D0D)

/** Banner / 高亮描边等荧光绿点缀 */
val BencaoBrandAccent = Color(0xFF76FF03)

val BencaoSecondary = Color(0xFF556B5F)      // muted herbal gray-green
val BencaoSecondaryOn = Color(0xFFFFFFFF)
val BencaoSecondaryContainer = Color(0xFFD6E8DD)
val BencaoSecondaryOnContainer = Color(0xFF101F19)

val BencaoTertiary = Color(0xFF3B6A8A)       // dew blue (subtle accent)
val BencaoTertiaryOn = Color(0xFFFFFFFF)
val BencaoTertiaryContainer = Color(0xFFD1E9FF)
val BencaoTertiaryOnContainer = Color(0xFF0A2030)

val BencaoBackground = Color(0xFFF5FAF3)     // slight green mist
val BencaoSurface = Color(0xFFF5FAF3)
val BencaoSurfaceVariant = Color(0xFFE0EBDD)
val BencaoOnBackground = Color(0xFF18211D)   // deep green-gray (not pure black)
val BencaoOnSurface = Color(0xFF18211D)
val BencaoOnSurfaceVariant = Color(0xFF3E4A44)
val BencaoOutline = Color(0xFF73807A)
val BencaoOutlineVariant = Color(0xFFC4CDC7)

val BencaoBackgroundDark = Color(0xFF0F1512) // deep green-black (not pure black)
val BencaoSurfaceDark = Color(0xFF0F1512)
val BencaoSurfaceVariantDark = Color(0xFF3A4640)
val BencaoOnBackgroundDark = Color(0xFFEAF3EE)
val BencaoOnSurfaceDark = Color(0xFFEAF3EE)
val BencaoOnSurfaceVariantDark = Color(0xFFC0CCC6)
val BencaoOutlineDark = Color(0xFF8C9A93)
val BencaoOutlineVariantDark = Color(0xFF55635C)

/** 暗色主题顶栏/主按钮绿（偏亮）；其上内容色为 [BencaoOnPrimaryDark]，勿再叠纯白字。 */
val BencaoPrimaryDark = Color(0xFF9AE06B)
val BencaoOnPrimaryDark = Color(0xFF1E3D0F)
val BencaoPrimaryContainerDark = Color(0xFF3D5C2E)
val BencaoOnPrimaryContainerDark = Color(0xFFD8F5C8)

val BencaoSecondaryDark = Color(0xFFB3CCC0)
val BencaoOnSecondaryDark = Color(0xFF1F352C)
val BencaoSecondaryContainerDark = Color(0xFF364B42)
val BencaoOnSecondaryContainerDark = Color(0xFFD0E8DC)

val BencaoTertiaryDark = Color(0xFFA7CFF0)
val BencaoOnTertiaryDark = Color(0xFF0E3147)
val BencaoTertiaryContainerDark = Color(0xFF284A63)
val BencaoOnTertiaryContainerDark = Color(0xFFD0E9FF)