package com.example.finmate.components

import com.example.finmate.model.Expenses
import com.google.firebase.firestore.FirebaseFirestore

fun fetchExpensesByCategory(
    category: String,
    onSuccess: (List<Expenses>) -> Unit,
    onFailure: (Exception) -> Unit
) {
    FirebaseFirestore.getInstance()
        .collection("expenses")
        .whereEqualTo("category", category)
        .get()
        .addOnSuccessListener { result ->
            val expenses = result.documents.mapNotNull { it.toObject(Expenses::class.java) }
            onSuccess(expenses)
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}
