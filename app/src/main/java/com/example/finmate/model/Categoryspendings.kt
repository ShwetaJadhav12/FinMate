package com.example.finmate.model

import androidx.compose.ui.graphics.Color

data class CategorySpending(
    val category: String,
    val amount: Float, // Using Float for drawing calculations
    val color: Color   // Color for the pie slice
)