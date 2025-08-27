package com.example.finmate.model

import androidx.compose.ui.graphics.Color

data class CategoryExpense(
    val category: String,
    val amount: Int, // Using Float for drawing calculations
    val color: Color ,  // Color for the pie slice
    val date: String
)