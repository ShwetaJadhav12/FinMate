package com.example.finmate.model

data class Expenses(
    val id: String = "",          // Firestore document ID
    val title: String = "",
    val amount: String = "",
    val category: String = "",
    val date: String = "",
    val time: String = ""
)
