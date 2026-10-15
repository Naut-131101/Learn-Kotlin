package com.example.businesscard

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Email
//import androidx.compose.material.icons.filled.Phone
//import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.businesscard.ui.theme.BusinessCardTheme
import com.example.businesscard.ui.theme.cardColors

@Composable
fun ContactRow(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    modifier: Modifier = Modifier,
    iconDescription: String? = null,
) {
    Row(
        modifier = modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconDescription,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ProfileHeader(
    name: String,
    title: String,
    organization: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.avatar),
            contentDescription = stringResource(R.string.cd_avatar),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(width = 3.dp, color = accentColor, shape = CircleShape)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = organization,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun BusinessCard(modifier: Modifier = Modifier) {
    val colors = cardColors()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
        contentColor = colors.content
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()          // tránh thanh trạng thái, thanh điều hướng, tai thỏ
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            ProfileHeader(
                name = stringResource(R.string.full_name),
                title = stringResource(R.string.job_title),
                organization = stringResource(R.string.organization),
                accentColor = colors.accent
            )
            Spacer(Modifier.weight(1f))
            ContactSection(accentColor = colors.accent)
        }
    }
}

@Composable
private fun ContactSection(accentColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        HorizontalDivider(
            color = accentColor.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
//        ContactRow(
//            icon = Icons.Default.Phone,
//            text = stringResource(R.string.phone_number),
//            iconTint = accentColor,
//            iconDescription = stringResource(R.string.cd_phone)
//        )
//        ContactRow(
//            icon = Icons.Default.Email,
//            text = stringResource(R.string.email),
//            iconTint = accentColor,
//            iconDescription = stringResource(R.string.cd_email)
//        )
//        ContactRow(
//            icon = Icons.Default.Share,
//            text = stringResource(R.string.social_handle),
//            iconTint = accentColor,
//            iconDescription = stringResource(R.string.cd_social)
//        )
    }
}

@Preview(name = "Light", showBackground = true, showSystemUi = true)
@Composable
private fun BusinessCardLightPreview() {
    BusinessCardTheme {
        BusinessCard()
    }
}

@Preview(
    name = "Dark",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun BusinessCardDarkPreview() {
    BusinessCardTheme {
        BusinessCard()
    }
}

@Preview(showBackground = true)
@Composable
private fun ContactRowPreview() {
    BusinessCardTheme {
        Surface {
//            ContactRow(
//                icon = Icons.Default.Email,
//                text = "preview@example.com",
//                iconTint = MaterialTheme.colorScheme.primary
//            )
        }
    }
}

