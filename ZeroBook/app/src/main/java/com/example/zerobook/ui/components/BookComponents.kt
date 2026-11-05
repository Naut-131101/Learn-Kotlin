package com.example.zerobook.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.zerobook.R
import com.example.zerobook.model.Book
import com.example.zerobook.model.ReadingStatus

@StringRes
fun ReadingStatus.labelRes(): Int = when (this) {
    ReadingStatus.READING -> R.string.status_reading
    ReadingStatus.WANT_TO_READ -> R.string.status_want_to_read
    ReadingStatus.READ -> R.string.status_read
}

/** Ảnh bìa sách; nếu không có ảnh thì hiện icon cuốn sách. */
@Composable
fun BookCover(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Một dòng sách trong danh sách: ảnh bìa + tên + tác giả (+ nội dung phụ tuỳ chọn). */
@Composable
fun BookListItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookCover(
            imageUrl = book.thumbnailUrl,
            contentDescription = null, // tên sách đã được đọc bởi Text bên cạnh
            modifier = Modifier.size(width = 56.dp, height = 84.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (book.authors.isNotEmpty()) {
                Text(
                    text = book.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (supportingContent != null) {
                Spacer(Modifier.height(4.dp))
                supportingContent()
            }
        }
    }
}

/**
 * Thanh 5 sao. Truyền [onRatingChange] = null để chỉ hiển thị (không bấm được).
 */
@Composable
fun RatingBar(
    rating: Int,
    onRatingChange: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    starSize: Dp = 32.dp,
) {
    val summary = stringResource(R.string.rating_summary, rating)
    val rowModifier = if (onRatingChange == null) {
        // Chế độ chỉ xem: TalkBack đọc "4 trên 5 sao" thay vì đọc từng ngôi sao
        modifier.semantics(mergeDescendants = true) { contentDescription = summary }
    } else {
        modifier
    }
    Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
        for (star in 1..5) {
            val icon = if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder
            val tint = if (star <= rating) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            }
            if (onRatingChange != null) {
                IconButton(onClick = { onRatingChange(star) }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(R.string.rating_star_description, star),
                        tint = tint,
                        modifier = Modifier.size(starSize),
                    )
                }
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(starSize),
                )
            }
        }
    }
}
