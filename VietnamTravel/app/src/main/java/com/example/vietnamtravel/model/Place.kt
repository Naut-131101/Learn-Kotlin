package com.example.vietnamtravel.model

import androidx.annotation.DrawableRes

enum class Region(val label: String) {
    NORTH("Miền Bắc"),
    CENTRAL("Miền Trung"),
    SOUTH("Miền Nam"),
}

data class Place(
    val id: Int,
    val name: String,
    val province: String,
    val region: Region,
    @DrawableRes val imageRes: Int,
    val rating: Double,
    val shortDescription: String,
)