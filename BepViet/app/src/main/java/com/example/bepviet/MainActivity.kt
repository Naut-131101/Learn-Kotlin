package com.example.bepviet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bepviet.navigation.BepVietNavHost
import com.example.bepviet.ui.theme.BepVietTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BepVietTheme {
                BepVietNavHost()
            }
        }
    }
}