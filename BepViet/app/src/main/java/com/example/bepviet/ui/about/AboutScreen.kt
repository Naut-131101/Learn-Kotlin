package com.example.bepviet.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bepviet.data.RecipeRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(bottomBar: @Composable () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Giới thiệu") }) },
        bottomBar = bottomBar,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("🍲 Bếp Việt", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Sổ tay ${RecipeRepository.recipes.size} công thức món Việt, " +
                        "viết bằng Kotlin và Jetpack Compose trong khoá Lập trình Android.",
            )
            Text("Sinh viên: Nguyễn Văn A")   // ← sửa thành tên của bạn
            Text("MSSV: 123456")               // ← sửa thành MSSV của bạn
            Text("Phiên bản: tuần 6", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}