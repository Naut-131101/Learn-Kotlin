package com.example.zerobook.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Bảng màu dự phòng (xanh rêu) cho máy Android < 12 không hỗ trợ Dynamic Color
private val LightColors = lightColorScheme(
    primary = Color(0xFF3E5F4A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC0EBCB),
    onPrimaryContainer = Color(0xFF002111),
    secondary = Color(0xFF8B5A2B),
    onSecondary = Color(0xFFFFFFFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA5D0B0),
    onPrimary = Color(0xFF0B3720),
    primaryContainer = Color(0xFF264A33),
    onPrimaryContainer = Color(0xFFC0EBCB),
    secondary = Color(0xFFFFB77C),
    onSecondary = Color(0xFF4A2800),
)

@Composable
fun ZeroBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        // Dynamic Color: lấy màu theo hình nền của máy (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
