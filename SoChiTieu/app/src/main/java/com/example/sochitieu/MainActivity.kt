package com.example.sochitieu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sochitieu.ui.ExpenseScreen
import com.example.sochitieu.ui.theme.SoChiTieuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoChiTieuTheme {
                ExpenseScreen()
            }
        }
    }
}