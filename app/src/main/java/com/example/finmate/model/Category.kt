package com.example.finmate.model

data class Category(
    val id: String = "",
    val name: String = "",
    val imageurl: String = ""
)
data class MonthSummary(
    val expenses: List<CategoryExpense> = emptyList(),
    val budget: Float? = null,
    val remaining: Float? = null
)
