package com.example.vietnamtravel.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vietnamtravel.R
import com.example.vietnamtravel.data.DataSource
import com.example.vietnamtravel.model.Place
import com.example.vietnamtravel.ui.components.FeaturedPlaceCard
import com.example.vietnamtravel.ui.components.PlaceCard
import com.example.vietnamtravel.ui.theme.VietnamTravelTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(places: List<Place>, modifier: Modifier = Modifier) {
    // 5 địa điểm điểm cao nhất. Danh sách nhỏ nên tính trực tiếp là đủ;
    // tuần 5 bạn sẽ học remember để tránh tính lại không cần thiết.
    val featured = places.sortedByDescending { it.rating }.take(5)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionTitle(stringResource(R.string.section_featured))
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featured, key = { it.id }) { place ->
                        FeaturedPlaceCard(place)
                    }
                }
            }
            item {
                SectionTitle(stringResource(R.string.section_all_places))
            }
            items(places, key = { it.id }) { place ->
                PlaceCard(
                    place = place,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Preview(name = "Light", showBackground = true, showSystemUi = true)
@Composable
private fun TravelScreenLightPreview() {
    VietnamTravelTheme { TravelScreen(DataSource.places) }
}

@Preview(
    name = "Dark",
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun TravelScreenDarkPreview() {
    VietnamTravelTheme { TravelScreen(DataSource.places) }
}