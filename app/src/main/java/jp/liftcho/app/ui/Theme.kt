package jp.liftcho.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1C5B55),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE9E6),
    onPrimaryContainer = Color(0xFF07201D),
    secondary = Color(0xFF5C6366),
    background = Color(0xFFF4F3EF),
    onBackground = Color(0xFF16191A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16191A),
    surfaceVariant = Color(0xFFEBE9E2),
    onSurfaceVariant = Color(0xFF4C5254),
    error = Color(0xFF9B382B)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF72C0B2),
    onPrimary = Color(0xFF07201D),
    primaryContainer = Color(0xFF16302D),
    onPrimaryContainer = Color(0xFFCDE8E2),
    secondary = Color(0xFF99A1A3),
    background = Color(0xFF121415),
    onBackground = Color(0xFFE8EAE7),
    surface = Color(0xFF191C1E),
    onSurface = Color(0xFFE8EAE7),
    surfaceVariant = Color(0xFF1F2325),
    onSurfaceVariant = Color(0xFFB4BBBD),
    error = Color(0xFFE8897A)
)

@Composable
fun LiftchoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
