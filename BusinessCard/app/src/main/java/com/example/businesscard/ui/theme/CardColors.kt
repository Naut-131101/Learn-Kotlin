package com.example.businesscard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class CardColors(
    val background: Color,
    val content: Color,
    val accent: Color,
)

@Composable
fun cardColors(darkTheme: Boolean = isSystemInDarkTheme()): CardColors =
    if (darkTheme) {
        CardColors(CardBackgroundDark, CardContentDark, CardAccentDark)
    } else {
        CardColors(CardBackgroundLight, CardContentLight, CardAccentLight)
    }