package com.example.bepviet.ui.recipes.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bepviet.data.RecipeData
import com.example.bepviet.ui.components.RecipeCard
import com.example.bepviet.ui.theme.BepVietTheme

@Composable
fun RecipeListRoute(
    onRecipeClick: (Int) -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: RecipeListViewModel = viewModel(factory = RecipeListViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    RecipeListScreen(
        uiState = uiState,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRecipeClick = onRecipeClick,
        bottomBar = bottomBar,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    uiState: RecipeListUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onRecipeClick: (Int) -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Bếp Việt") }) },
        bottomBar = bottomBar,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SearchField(query = query, onQueryChange = onQueryChange)

            when (uiState) {
                RecipeListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is RecipeListUiState.Success -> {
                    CategoryChips(
                        categories = uiState.categories,
                        selected = uiState.selectedCategory,
                        onSelect = onCategorySelect,
                    )
                    if (uiState.recipes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Không tìm thấy món nào 🍽️")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.recipes, key = { it.id }) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    isFavorite = recipe.id in uiState.favoriteIds,
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
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Tìm món, ví dụ: phở") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Xoá tìm kiếm")
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("Tất cả") })
        }
        items(categories) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeListLoadingPreview() {
    BepVietTheme {
        RecipeListScreen(
            uiState = RecipeListUiState.Loading,
            query = "",
            onQueryChange = {}, onCategorySelect = {}, onToggleFavorite = {}, onRecipeClick = {},
            bottomBar = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeListSuccessPreview() {
    BepVietTheme {
        RecipeListScreen(
            uiState = RecipeListUiState.Success(
                recipes = RecipeData.recipes.take(3),
                favoriteIds = setOf(1),
                categories = listOf("Món nước", "Món cơm"),
                selectedCategory = "Món nước",
            ),
            query = "",
            onQueryChange = {}, onCategorySelect = {}, onToggleFavorite = {}, onRecipeClick = {},
            bottomBar = {},
        )
    }
}