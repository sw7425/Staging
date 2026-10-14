package com.kpn.mobile.ui

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

/** Website tokens: #28A745 / #0066CC, neutral borders, 4–8px corners, system sans. */
object KpnPalette {
    val Brand = Color(0xFF28A745)
    val Action = Color(0xFF176B36) // darker brand derivative: accessible white action text
    val Blue = Color(0xFF0066CC)
    val Ink = Color(0xFF17251C)
    val Muted = Color(0xFF57635B)
    val Border = Color(0xFFDCE3DE)
    val Warning = Color(0xFF845C12)
    val Danger = Color(0xFFAF2E30)
}
private val Light = lightColorScheme(
    primary = KpnPalette.Action, onPrimary = Color.White,
    primaryContainer = Color(0xFFEAF5ED), onPrimaryContainer = Color(0xFF174A29),
    secondary = KpnPalette.Blue, onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAF1FB), onSecondaryContainer = Color(0xFF154779),
    tertiary = KpnPalette.Warning, onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF3D9), onTertiaryContainer = Color(0xFF60450D),
    background = Color(0xFFF6F8F6), onBackground = KpnPalette.Ink,
    surface = Color.White, onSurface = KpnPalette.Ink,
    surfaceVariant = Color(0xFFF0F3F1), onSurfaceVariant = KpnPalette.Muted,
    surfaceContainer = Color(0xFFF0F3F1), surfaceContainerLow = Color(0xFFF8FAF8),
    surfaceContainerHigh = Color(0xFFE8EEEA), surfaceContainerHighest = Color(0xFFE3EAE5),
    outline = Color(0xFF7C8980), outlineVariant = KpnPalette.Border,
    error = KpnPalette.Danger, onError = Color.White,
    errorContainer = Color(0xFFFCECEC), onErrorContainer = Color(0xFF762124),
)
private val Dark = darkColorScheme(
    primary = Color(0xFF91D6A5), onPrimary = Color(0xFF09341A),
    primaryContainer = Color(0xFF183D27), onPrimaryContainer = Color(0xFFD3EEDB),
    secondary = Color(0xFF9DC5F5), onSecondary = Color(0xFF102F52),
    secondaryContainer = Color(0xFF193B63), onSecondaryContainer = Color(0xFFD5E5F8),
    tertiary = Color(0xFFE4C486), onTertiary = Color(0xFF392903),
    tertiaryContainer = Color(0xFF453715), onTertiaryContainer = Color(0xFFF6E7C7),
    background = Color(0xFF121A15), onBackground = Color(0xFFE6EBE7),
    surface = Color(0xFF19221C), onSurface = Color(0xFFE6EBE7),
    surfaceVariant = Color(0xFF263029), onSurfaceVariant = Color(0xFFBAC6BD),
    surfaceContainer = Color(0xFF202B23), surfaceContainerLow = Color(0xFF18211B),
    outline = Color(0xFF8A998E), outlineVariant = Color(0xFF3B483E),
    error = Color(0xFFF3AFAD), errorContainer = Color(0xFF552427), onErrorContainer = Color(0xFFF9DAD9),
)
private fun type(size: Int, line: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = size.sp, lineHeight = line.sp, fontWeight = weight)
private val Type = Typography(
    displaySmall = type(28, 34, FontWeight.Bold), headlineMedium = type(23, 30, FontWeight.SemiBold),
    headlineSmall = type(21, 28, FontWeight.SemiBold), titleLarge = type(19, 26, FontWeight.SemiBold),
    titleMedium = type(16, 23, FontWeight.SemiBold), titleSmall = type(14, 21, FontWeight.SemiBold),
    bodyLarge = type(16, 25), bodyMedium = type(15, 23), bodySmall = type(14, 21),
    labelLarge = type(14, 20, FontWeight.Medium), labelMedium = type(13, 18, FontWeight.Medium), labelSmall = type(12, 17, FontWeight.Medium),
)
/** Semantic foreground/background pairs follow the active palette, including dark mode. */
internal enum class StatusTone { SUCCESS, WARNING, DANGER, INFO, NEUTRAL }
internal fun statusTone(status: String): StatusTone = when (status.uppercase()) {
    "VERIFIED", "APPROVED", "OPEN", "PUBLISHED", "ACTIVE", "ONGOING" -> StatusTone.SUCCESS
    "UNDER_REVIEW", "SUBMITTED", "PENDING", "FLAGGED", "ESCALATED" -> StatusTone.WARNING
    "REJECTED", "DISMISSED", "SUSPENDED" -> StatusTone.DANGER
    "PLANNED" -> StatusTone.INFO
    else -> StatusTone.NEUTRAL
}
@Composable internal fun statusColors(tone: StatusTone): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    return when (tone) {
        StatusTone.SUCCESS -> colors.onPrimaryContainer to colors.primaryContainer
        StatusTone.WARNING -> colors.onTertiaryContainer to colors.tertiaryContainer
        StatusTone.DANGER -> colors.onErrorContainer to colors.errorContainer
        StatusTone.INFO -> colors.onSecondaryContainer to colors.secondaryContainer
        StatusTone.NEUTRAL -> colors.onSurfaceVariant to colors.surfaceVariant
    }
}
@Composable fun KpnTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Dark else Light, typography = Type,
        shapes = Shapes(extraSmall = RoundedCornerShape(4.dp), small = RoundedCornerShape(6.dp), medium = RoundedCornerShape(8.dp), large = RoundedCornerShape(12.dp), extraLarge = RoundedCornerShape(16.dp)), content = content)
}
