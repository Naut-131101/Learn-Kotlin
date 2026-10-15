package com.example.vietnamtravel.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.vietnamtravel.ui.theme.StarColor
import kotlin.math.roundToInt

@Composable
fun RatingBar(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp,
) {
    val filledStars = rating.roundToInt().coerceIn(0, 5)
    val ratingText = "%.1f".format(rating)

    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Đánh giá $ratingText trên 5 sao"
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < filledStars) StarColor
                else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(starSize)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = ratingText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}