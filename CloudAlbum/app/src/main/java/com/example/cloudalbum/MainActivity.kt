package com.example.cloudalbum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cloudalbum.ui.AlbumScreen
import com.example.cloudalbum.ui.theme.CloudAlbumTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudAlbumTheme {
                AlbumScreen()
            }
        }
    }
}