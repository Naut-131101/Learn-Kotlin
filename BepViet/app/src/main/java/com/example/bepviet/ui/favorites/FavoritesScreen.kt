package com.example.bepviet.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bepviet.ui.components.RecipeCard
import com.example.bepviet.ui.theme.BepVietTheme

@Composable
fun FavoritesRoute(
    onRecipeClick: (Int) -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesScreen(
        uiState = uiState,
        onRecipeClick = onRecipeClick,
        onToggleFavorite = viewModel::onToggleFavorite,
        bottomBar = bottomBar,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    uiState: FavoritesUiState,
    onRecipeClick: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Món yêu thích") }) },
        bottomBar = bottomBar,
    ) { innerPadding ->
        val contentModifier = Modifier.fillMaxSize().padding(innerPadding)
        when (uiState) {
            FavoritesUiState.Loading -> Box(contentModifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is FavoritesUiState.Success -> {
                if (uiState.recipes.isEmpty()) {
                    Box(contentModifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Chưa có món yêu thích.\nBấm biểu tượng ♡ ở danh sách hoặc trang chi tiết để lưu món.",
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = contentModifier,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.recipes, key = { it.id }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                isFavorite = true,
                                onClick = { onRecipeClick(recipe.id) },
                                onToggleFavorite = { onToggleFavorite(recipe.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesEmptyPreview() {
    BepVietTheme {
        FavoritesScreen(
            uiState = FavoritesUiState.Success(emptyList()),
            onRecipeClick = {}, onToggleFavorite = {}, bottomBar = {},
        )
    }
}