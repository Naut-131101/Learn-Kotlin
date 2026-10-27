package com.example.bepviet.ui.recipes.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bepviet.data.Recipe
import com.example.bepviet.data.RecipeData
import com.example.bepviet.ui.components.FavoriteButton
import com.example.bepviet.ui.theme.BepVietTheme

@Composable
fun RecipeDetailRoute(
    onBack: () -> Unit,
    viewModel: RecipeDetailViewModel = viewModel(factory = RecipeDetailViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecipeDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    uiState: RecipeDetailUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState) {
                            RecipeDetailUiState.Loading -> ""
                            RecipeDetailUiState.NotFound -> "Không tìm thấy"
                            is RecipeDetailUiState.Success -> uiState.recipe.name
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (uiState is RecipeDetailUiState.Success) {
                        FavoriteButton(isFavorite = uiState.isFavorite, onClick = onToggleFavorite)
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            RecipeDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            RecipeDetailUiState.NotFound -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text("Công thức không tồn tại 😢")
                }
            }
            is RecipeDetailUiState.Success -> {
                RecipeDetailContent(recipe = uiState.recipe, modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Composable
fun RecipeDetailContent(recipe: Recipe, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(recipe.emoji, fontSize = 72.sp)
                Text(recipe.name, style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "${recipe.category} · ${recipe.cookTimeMin} phút · ${recipe.difficulty.label}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(8.dp))
                Text(recipe.description, style = MaterialTheme.typography.bodyLarge)
            }
        }
        item { SectionTitle("Nguyên liệu") }
        items(recipe.ingredients) { ingredient ->
            Text("• $ingredient", style = MaterialTheme.typography.bodyLarge)
        }
        item { SectionTitle("Các bước") }
        itemsIndexed(recipe.steps) { index, step ->
            StepRow(number = index + 1, text = step)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp),
    )
}

@Composable
private fun StepRow(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$number", color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeDetailPreview() {
    BepVietTheme {
        RecipeDetailScreen(
            uiState = RecipeDetailUiState.Success(RecipeData.recipes.first(), isFavorite = true),
            onBack = {},
            onToggleFavorite = {},
        )
    }
}