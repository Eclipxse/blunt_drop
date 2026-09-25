package com.example.blunt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DropOrange = Color(0xFFFF6B35)
val StudioInk = Color(0xFF20211F)
private val Light = lightColorScheme(
    primary = Color(0xFFAD380C), onPrimary = Color.White, primaryContainer = Color(0xFFFFE2D5), onPrimaryContainer = Color(0xFF782300),
    secondary = StudioInk, onSecondary = Color.White, secondaryContainer = Color(0xFFEAECE6), onSecondaryContainer = StudioInk,
    tertiary = Color(0xFF32644B), onTertiary = Color.White, tertiaryContainer = Color(0xFFE0EEDF), onTertiaryContainer = Color(0xFF234832),
    background = Color(0xFFFAF9F6), onBackground = StudioInk, surface = Color(0xFFFAF9F6), onSurface = StudioInk,
    surfaceVariant = Color(0xFFF0EFEB), onSurfaceVariant = Color(0xFF62645E),
    surfaceContainer = Color(0xFFF0EFEB), surfaceContainerLow = Color(0xFFF6F5F1), surfaceContainerHigh = Color(0xFFE9E9E4),
    outline = Color(0xFF797B75), outlineVariant = Color(0xFFDCDED6), inverseSurface = StudioInk, inverseOnSurface = Color(0xFFFAF9F6)
)
private val Dark = darkColorScheme(
    primary = Color(0xFFFFAE8D), onPrimary = Color(0xFF5F1900), primaryContainer = Color(0xFF6E290D), onPrimaryContainer = Color(0xFFFFDBCC),
    secondary = Color(0xFFE4E6DE), onSecondary = StudioInk, secondaryContainer = Color(0xFF343730), onSecondaryContainer = Color(0xFFE4E6DE),
    tertiary = Color(0xFFA5D5B4), onTertiary = Color(0xFF103923), tertiaryContainer = Color(0xFF274B34), onTertiaryContainer = Color(0xFFD8EDDA),
    background = Color(0xFF151714), onBackground = Color(0xFFF1F1EB), surface = Color(0xFF151714), onSurface = Color(0xFFF1F1EB),
    surfaceVariant = Color(0xFF282B26), onSurfaceVariant = Color(0xFFC0C4B9),
    surfaceContainer = Color(0xFF242721), surfaceContainerLow = Color(0xFF1C1F19), surfaceContainerHigh = Color(0xFF30342C),
    outline = Color(0xFF93978B), outlineVariant = Color(0xFF41463B), inverseSurface = Color(0xFFE9ECE2), inverseOnSurface = StudioInk
)
private fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Normal, tracking: Float = 0f) = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = weight, fontSize = size.sp, lineHeight = line.sp, letterSpacing = tracking.sp)
private val BluntTypography = Typography(
    displayLarge = style(56, 58, FontWeight.Bold, -2f), displayMedium = style(44, 46, FontWeight.Bold, -1.5f), displaySmall = style(36, 40, FontWeight.Bold, -1f),
    headlineLarge = style(32, 36, FontWeight.Bold, -.8f), headlineMedium = style(28, 32, FontWeight.Bold, -.6f), headlineSmall = style(24, 28, FontWeight.SemiBold, -.4f),
    titleLarge = style(20, 26, FontWeight.SemiBold, -.3f), titleMedium = style(16, 22, FontWeight.SemiBold), titleSmall = style(14, 20, FontWeight.Medium),
    bodyLarge = style(16, 25), bodyMedium = style(14, 21), bodySmall = style(12, 18),
    labelLarge = style(14, 20, FontWeight.SemiBold), labelMedium = style(12, 16, FontWeight.SemiBold, .3f), labelSmall = style(11, 16, FontWeight.Medium, .4f)
)
@Composable fun BluntTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, typography = BluntTypography,
        shapes = Shapes(extraSmall = RoundedCornerShape(6.dp), small = RoundedCornerShape(8.dp), medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(16.dp), extraLarge = RoundedCornerShape(24.dp)), content = content)
}
object Space { val page = 24.dp; val section = 32.dp; val gap = 16.dp; val small = 8.dp }
